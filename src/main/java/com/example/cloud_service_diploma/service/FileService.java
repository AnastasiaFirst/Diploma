package com.example.cloud_service_diploma.service;

import com.example.cloud_service_diploma.exception.*;
import com.example.cloud_service_diploma.model.File;
import com.example.cloud_service_diploma.entity.FileEntity;
import com.example.cloud_service_diploma.entity.UserEntity;
import com.example.cloud_service_diploma.model.dto.FileDTO;
import com.example.cloud_service_diploma.repositories.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FileService {
    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final FileRepository fileRepository;

    @Autowired
    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Transactional
    public File addFile(MultipartFile file, String fileName, Long userID) throws IOException {

        log.info("Поиск файла в хранилище по имени файла {} и идентификатору {}", fileName, userID);
        if (fileName.contains(",")) {
            fileName = fileName.split(",")[0];
        }

        LocalDateTime fileUploadDate = LocalDateTime.now();
        byte[] fileBytes = file.getBytes();
        if (fileBytes.length == 0) {
            throw new ErrorInputData("Файл не должен быть пустым.", 400);
        }
        FileEntity createdFile = FileEntity.build(UserEntity.build(userID), fileName, file.getName(), file.getBytes(), file.getSize(), fileUploadDate);

        log.info("Файл создан и сохранен в базе данных. Имя файла: {}, Id пользователя: {}", fileName, userID);
        fileRepository.save(createdFile);
        return File.build(file.getOriginalFilename(), fileBytes);
    }

    @Transactional
    public File getFile(String fileName, Long userID) throws IOException {

        log.info("Поиск файла в базе данных по имени файла: {} и Id пользователя: {}", fileName, userID);
        try {
            FileEntity file = fileRepository.findFileByUserEntityIdAndFileName(userID, fileName)
                    .orElseThrow(() -> new ErrorInputData("Файл с именем: { " + fileName + " } не найден", 400));
            log.info("Загрузка файла: {} из базы данных. Id пользователя: {}", fileName, userID);

            return File.build(file.getHash(), file.getFile());
        } catch (ErrorUploadFile e) {
            log.error("Ошибка загрузки файла: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Произошла ошибка при получении файла: {}", e.getMessage(), e);
            throw new ErrorUploadFile("Произошла ошибка при обработке запроса. Пожалуйста, попробуйте позже.", 500);
        }
    }

    @Transactional
    public ResponseEntity<String> renameFile(String fileName, String newFileName, Long userID) {

        if (newFileName == null || newFileName.isEmpty()) {
            throw new ErrorInputData("Некорректные входные данные: новое имя файла не может быть пустым", 400);
        }

        log.info("Проверка нового имени файла в базе данных по имени файла: {} и Id пользователя: {}", newFileName, userID);
        if (fileRepository.findFileByUserEntityIdAndFileName(userID, newFileName).isPresent()) {
            throw new ErrorUploadFile("Файл с таким именем: {" + newFileName + "} уже существует", HttpStatus.CONFLICT.value());
        }

        Optional<FileEntity> fileToRenameOpt = fileRepository.findFileByUserEntityIdAndFileName(userID, fileName);

        if (!fileToRenameOpt.isPresent()) {
            throw new ErrorUploadFile("Файл с именем: {" + fileName + "} не найден", 500);
        }

        FileEntity fileToRename = fileToRenameOpt.get();
        log.info("Переименование файла в базе данных. Старое имя файла: {}, новое имя файла: {}, Id пользователя: {}",
                fileName, newFileName, userID);

        fileToRename.setFileName(newFileName);
        fileRepository.save(fileToRename);

        return ResponseEntity.ok("У файла с именем " + fileName + " успешно изменено имя на " + newFileName + ".");
    }

    @Transactional
    public void deleteFile(String fileName, Long userID) {

        log.info("Поиск файла для удаления в базе данных по имени файла: {} и Id пользователя: {}", fileName, userID);
        FileEntity fileFromStorage = fileRepository.findFileByUserEntityIdAndFileName(userID, fileName)
                .orElseThrow(() -> new ErrorInputData("Файл с именем: { " + fileName + " } не найден", 400));

        log.info("Удаление файла из базы данных по имени файла: {} и Id пользователя: {}", fileFromStorage, userID);
        try {
            fileRepository.deleteById(fileFromStorage.getId());
            log.info("Файл с именем: {} успешно удален.", fileName);
        } catch (EmptyResultDataAccessException e) {
            log.error("Ошибка при удалении файла: {}", e.getMessage());
            throw new ErrorDeleteFile("Не удалось удалить файл с именем: " + fileName, 500);
        }
    }
    @Transactional
    public List<FileDTO> getAllFiles(int limit, Long userID) {

        if (limit <= 0) {
            throw new ErrorInputData("Лимит должен быть больше нуля.", 400);
        }

        log.info("Поиск всех файлов в базе данных по Id пользователя: {} и лимиту вывода: {}", userID, limit);
        List<FileEntity> listFiles;
        try {
            Pageable pageable = PageRequest.of(0, limit);
            listFiles = fileRepository.findByUserEntityId(userID, pageable);
        } catch (Exception e) {
            log.error("Ошибка при получении списка файлов для пользователя {}: {}", userID, e.getMessage());
            throw new ErrorGettingList("Не удалось получить список файлов.", 500);
        }

        log.info("Все файлы в базе данных по Id пользователя: {} и лимиту вывода: {} найдены | Список файлов: {}", userID, limit, listFiles);
        return listFiles.stream()
                .map(file -> FileDTO.create(file.getFileName(), file.getSize()))
                .collect(Collectors.toList());
    }
}
