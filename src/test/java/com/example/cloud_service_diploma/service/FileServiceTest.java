package com.example.cloud_service_diploma.service;

import com.example.cloud_service_diploma.entity.FileEntity;
import com.example.cloud_service_diploma.entity.UserEntity;
import com.example.cloud_service_diploma.exception.ErrorInputData;
import com.example.cloud_service_diploma.model.File;
import com.example.cloud_service_diploma.repositories.FileRepository;
import com.example.cloud_service_diploma.security.JWTToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private JWTToken jwtToken;

    @InjectMocks
    private FileService fileService;

    private Long userId = 1L;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("testFile.txt");
        when(mockFile.getBytes()).thenReturn("test content".getBytes());
        when(mockFile.getSize()).thenReturn(12L);

        when(jwtToken.getAuthenticatedUser ()).thenReturn(new UserEntity(userId));
        when(jwtToken.validateToken(anyString())).thenReturn(true);
    }

    @Test
    void testAddFileSuccess() throws IOException {
        String fileName = "testFile.txt";
        when(fileRepository.findFileByUserEntityIdAndFileName(userId, fileName)).thenReturn(Optional.empty());

        File result = fileService.addFile(mockFile, fileName, userId);

        assertNotNull(result);
        assertEquals("testFile.txt", result.getHash());
        verify(fileRepository).save(any(FileEntity.class));
    }

    @Test
    void testGetFileSuccess() throws IOException {
        String fileName = "testFile.txt";
        FileEntity fileEntity = new FileEntity();
        fileEntity.setHash("testFile.txt");
        fileEntity.setFile(mockFile.getBytes());

        when(fileRepository.findFileByUserEntityIdAndFileName(userId, fileName)).thenReturn(Optional.of(fileEntity));

        File result = fileService.getFile(fileName, userId);

        assertNotNull(result);
        assertEquals("testFile.txt", result.getHash());
    }

    @Test
    void testRenameFileSuccess() {
        String oldFileName = "oldFile.txt";
        String newFileName = "newFile.txt";

        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(1L);
        fileEntity.setFileName(oldFileName);

        when(fileRepository.findFileByUserEntityIdAndFileName(userId, oldFileName)).thenReturn(Optional.of(fileEntity));
        when(fileRepository.findFileByUserEntityIdAndFileName(userId, newFileName)).thenReturn(Optional.empty());

        ResponseEntity<String> responseEntity = fileService.renameFile(oldFileName, newFileName, userId);

        assertEquals("У файла с именем oldFile.txt успешно изменено имя на newFile.txt.", responseEntity.getBody());
        verify(fileRepository).save(fileEntity);
        assertEquals(newFileName, fileEntity.getFileName());
    }

    @Test
    void testRenameFileInvalidFileName() {
        String oldFileName = "oldFile.txt";
        String newFileName = null;

        ErrorInputData exception = assertThrows(ErrorInputData.class, () -> {
            fileService.renameFile(oldFileName, newFileName, userId);
        });
        assertEquals("Некорректные входные данные: новое имя файла не может быть пустым", exception.getMessage());
    }

    @Test
    void testDeleteFileSuccess() {
        String fileName = "testFile.txt";
        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(1L);
        fileEntity.setFileName(fileName);

        when(fileRepository.findFileByUserEntityIdAndFileName(userId, fileName)).thenReturn(Optional.of(fileEntity));

        assertDoesNotThrow(() -> fileService.deleteFile(fileName, userId));
        verify(fileRepository).deleteById(fileEntity.getId());
    }

    @Test
    void testDeleteFileNotFound() {
        String fileName = "nonExistentFile.txt";

        when(fileRepository.findFileByUserEntityIdAndFileName(userId, fileName)).thenReturn(Optional.empty());

        ErrorInputData exception = assertThrows(ErrorInputData.class, () -> {
            fileService.deleteFile(fileName, userId);
        });
        assertEquals("Файл с именем: { nonExistentFile.txt } не найден", exception.getMessage());
    }

    @Test
    void testGetAllFilesInvalidLimit() {
        int limit = 0;

        ErrorInputData exception = assertThrows(ErrorInputData.class, () -> {
            fileService.getAllFiles(limit, userId);
        });
        assertEquals("Лимит должен быть больше нуля.", exception.getMessage());
    }
}
