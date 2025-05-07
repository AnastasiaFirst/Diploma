package com.example.cloud_service_diploma.service;

import com.example.cloud_service_diploma.entity.FileEntity;
import com.example.cloud_service_diploma.entity.UserEntity;
import com.example.cloud_service_diploma.exception.ErrorInputData;
import com.example.cloud_service_diploma.exception.UserNotAuthorized;
import com.example.cloud_service_diploma.model.File;
import com.example.cloud_service_diploma.model.dto.FileDTO;
import com.example.cloud_service_diploma.repositories.FileRepository;
import com.example.cloud_service_diploma.security.JWTToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private JWTToken jwtToken;

    @InjectMocks
    private FileService fileService;

    private Long userId = 1L;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        String authToken = "mockToken";

        mockFile = mock(MultipartFile.class);
        when(mockFile.getName()).thenReturn("testFile.txt");
        when(mockFile.getBytes()).thenReturn("test content".getBytes());
        when(mockFile.getSize()).thenReturn(12L);

        when(jwtToken.getAuthenticatedUser()).thenReturn(new UserEntity(userId));
        when(jwtToken.validateToken(authToken)).thenReturn(true);
    }

    @Test
    void testAddFileSuccess() throws IOException {

        String authToken = "mockToken";
        String fileName = "testFile.txt";
        when(fileRepository.findFileByUserEntityIdAndFileName(userId, fileName)).thenReturn(Optional.empty());

        File result = fileService.addFile(mockFile, fileName, authToken);

        assertNotNull(result);
        assertEquals("testFile.txt", result.getHash());
        verify(fileRepository).save(any(FileEntity.class));
    }

    @Test
    void testAddFileUserNotAuthorized() {

        String authToken = "mockToken";
        when(jwtToken.getAuthenticatedUser()).thenReturn(null);

        UserNotAuthorized exception = assertThrows(UserNotAuthorized.class, () -> {
            fileService.addFile(mockFile, "file.txt", authToken);
        });
        assertEquals("Пользователь null не авторизован.", exception.getMessage());
    }

    @Test
    void testGetFileSuccess() throws IOException {
        String authToken = "mockToken";
        String fileName = "testFile.txt";
        FileEntity fileEntity = new FileEntity();
        fileEntity.setHash("testFile.txt");
        fileEntity.setFile(mockFile.getBytes());

        when(fileRepository.findFileByUserEntityIdAndFileName(userId, fileName)).thenReturn(Optional.of(fileEntity));

        File result = fileService.getFile(fileName, authToken);

        assertNotNull(result);
        assertEquals("testFile.txt", result.getHash());
    }
    @Test
    void testGetFileFileNotFound() {
        String authToken = "validToken";
        when(fileRepository.findFileByUserEntityIdAndFileName(userId, "nonExistentFile.txt")).thenReturn(Optional.empty());

        ErrorInputData exception = assertThrows(ErrorInputData.class, () -> {
            fileService.getFile("nonExistentFile.txt", authToken);
        });
        assertEquals("Файл с именем: { nonExistentFile.txt } не найден", exception.getMessage());
    }

    @Test
    void testRenameFileSuccess() {
        String oldFileName = "oldFile.txt";
        String newFileName = "newFile.txt";
        String authToken = "validToken";

        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(1L);
        fileEntity.setFileName(oldFileName);

        when(fileRepository.findFileByUserEntityIdAndFileName(userId, oldFileName)).thenReturn(Optional.of(fileEntity));
        when(fileRepository.findFileByUserEntityIdAndFileName(userId, newFileName)).thenReturn(Optional.empty());

        FileDTO fileDto = new FileDTO();
        fileDto.setFileName(newFileName);

        fileService.renameFile(oldFileName, fileDto, authToken);

        verify(fileRepository).save(fileEntity);
        assertEquals(newFileName, fileEntity.getFileName());
    }

    @Test
    void testRenameFileInvalidFileName() {
        String oldFileName = "oldFile.txt";
        String authToken = "validToken";
        FileDTO fileDto = new FileDTO();
        fileDto.setFileName("");

        ErrorInputData exception = assertThrows(ErrorInputData.class, () -> {
            fileService.renameFile(oldFileName, fileDto, authToken);
        });
        assertEquals("Некорректные входные данные: имя файла не может быть пустым", exception.getMessage());
    }

}