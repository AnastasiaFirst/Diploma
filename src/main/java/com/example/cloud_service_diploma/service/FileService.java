package com.example.cloud_service_diploma.service;

import com.example.cloud_service_diploma.exception.*;
import com.example.cloud_service_diploma.model.File;
import com.example.cloud_service_diploma.entity.FileEntity;
import com.example.cloud_service_diploma.entity.UserEntity;
import com.example.cloud_service_diploma.model.dto.FileDTO;
import com.example.cloud_service_diploma.repositories.FileRepository;
import com.example.cloud_service_diploma.security.JWTToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileService {
    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final FileRepository fileRepository;
    private final JWTToken jwtToken;

    @Autowired
    public FileService(FileRepository fileRepository, JWTToken jwtToken) {
        this.fileRepository = fileRepository;
        this.jwtToken = jwtToken;
    }

    @Transactional
    public File addFile(MultipartFile file, String fileName, String authToken) throws IOException {
        log.info("Получен токен: {}", authToken);
        Long userID = jwtToken.getAuthenticatedUser() != null ? jwtToken.getAuthenticatedUser().getId() : null;

        if (authToken == null || !authToken.startsWith("Bearer ")) {
            throw new UserNotAuthorized("Неавторизован", 401);
        }


        log.info("Поиск файла в хранилище по имени файла {} и идентификатору {}", fileName, userID);
        fileRepository.findFileByUserEntityIdAndFileName(userID, fileName).ifPresent(s -> {
            log.info("Файл не найден в базе данных по имени вайла {} и Id {}", fileName, userID);
            throw new ErrorInputData("Файл с таким именем: { " + fileName + " } уже существует для " + userID, 400);
        });

        log.info("Проверка наличия файла в базе данных по Id пользователя:{}", userID);
        if (fileRepository.findFileByUserEntityId(userID).isPresent()) {
            log.info("Файл у пользователя с Id: {} существует", userID);
            File myFile = File.build(file.getName(), file.getBytes());

        }
        log.info("Файл в базе данных по Id пользователя:{} не существует", userID);

        LocalDateTime fileUploadDate;
        fileUploadDate = LocalDateTime.now();
        FileEntity createdFile = FileEntity.build(UserEntity.build(userID), fileName, file.getName(), file.getBytes(), file.getSize(), fileUploadDate);

        log.info("Файл создан и сохранен в базе данных. Имя файла: {}, Id пользователя: {}", fileName, userID);
        fileRepository.save(createdFile);
        throw new SuccessUpload("Файл с именем " + fileName + " успешно добавлен.", 200);
    }

    @Transactional
    public File getFile(String fileName, String authToken) throws IOException {
        Long userID = jwtToken.getAuthenticatedUser() != null ? jwtToken.getAuthenticatedUser().getId() : null;

        if (authToken == null || !authToken.startsWith("Bearer ")) {
            throw new UserNotAuthorized("Неавторизован", 401);
        }

        String token = authToken.substring(7);

        if (!jwtToken.validateToken(token)) {
            throw new UserNotAuthorized("Неавторизован", 401);
        }

        log.info("Проверка пользователя на авторизацию: по Id пользователя: {}", userID);
        if (userID == null) {
            throw new UserNotAuthorized("Пользователь " + userID + " не авторизован.", 401);
        }

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

    public void renameFile(String fileName, FileDTO fileDto, String authToken) {
        Long userID = jwtToken.getAuthenticatedUser() != null ? jwtToken.getAuthenticatedUser().getId() : null;

        if (authToken == null || !authToken.startsWith("Bearer ")) {
            throw new UserNotAuthorized("Неавторизован", 401);
        }

        String token = authToken.substring(7);

        if (!jwtToken.validateToken(token)) {
            throw new UserNotAuthorized("Неавторизован", 401);
        }

        log.info("Проверка пользователя на авторизацию: по Id пользователя: {}", userID);
        if (userID == null) {
            throw new UserNotAuthorized("Пользователь " + userID + " не авторизован.", 401);
        }

        if (fileDto == null || fileDto.getFileName() == null || fileDto.getFileName().isEmpty()) {
            throw new ErrorInputData("Некорректные входные данные: имя файла не может быть пустым", 400);
        }

        log.info("Поиск файла для переименования в базе данных по имени файла: {} и Id пользователя: {}", fileName, userID);
        FileEntity fileToRename = fileRepository.findFileByUserEntityIdAndFileName(userID, fileName)
                .orElseThrow(() -> new ErrorUploadFile("Файл с именем: {" + fileName + "} не найден", 500));

        log.info("Проверка нового имени файла в базе данных по имени файла: {} и Id пользователя: {}", fileDto.getFileName(), userID);
        fileRepository.findFileByUserEntityIdAndFileName(userID, fileDto.getFileName()).ifPresent(s -> {
            log.info("Файл не найден в базе данных по имени вайла: {} и Id пользователя: {}", fileName, userID);
            throw new ErrorUploadFile("Файл с таким именем: { " + fileName + " } уже существует", 500);
        });

        log.info("Переименование файла в базе данных. Старое имя файла: {}, новое имя файла: {}, Id пользователя: {}",
                fileName, fileToRename, userID);
        fileToRename.setFileName(fileDto.getFileName());
        fileRepository.save(fileToRename);

        throw new SuccessUpload("У файла с именем " + fileName + " успешно изменено имя файла на "
                + fileDto.getFileName() + ".", 200);
    }

    public void deleteFile(String fileName, String authToken) {
        Long userID = jwtToken.getAuthenticatedUser() != null ? jwtToken.getAuthenticatedUser().getId() : null;

        if (authToken == null || !authToken.startsWith("Bearer ")) {
            throw new UserNotAuthorized("Неавторизован", 401);
        }

        String token = authToken.substring(7);

        if (!jwtToken.validateToken(token)) {
            throw new UserNotAuthorized("Неавторизован", 401);
        }

        log.info("Проверка пользователя на авторизацию: по Id пользователя: {}", userID);
        if (userID == null) {
            throw new UserNotAuthorized("Пользователь " + userID + " не авторизован.", 401);
        }

        log.info("Поиск файла для удаления в базе данных по имени файла: {} и Id пользователя: {}", fileName, userID);
        FileEntity fileFromStorage = fileRepository.findFileByUserEntityIdAndFileName(userID, fileName)
                .orElseThrow(() -> new ErrorInputData("Файл с именем: { " + fileName + " } не найден", 400));

        log.info("Удаление файла из базы данных по имени файла: {} и Id пользователя: {}", fileFromStorage, userID);
        try {
            fileRepository.deleteById(fileFromStorage.getId());
            throw new SuccessDeleted("Файл с именем: " + fileName + " успешно удален.", 200);
        } catch (Exception e) {
            log.error("Ошибка при удалении файла: {}", e.getMessage());
            throw new ErrorDeleteFile("Не удалось удалить файл с именем: " + fileName, 500);
        }
    }

    public List<FileDTO> getAllFiles(int limit, String authToken) {
        Long userID = jwtToken.getAuthenticatedUser() != null ? jwtToken.getAuthenticatedUser().getId() : null;

        if (authToken == null || !authToken.startsWith("Bearer ")) {
            throw new UserNotAuthorized("Неавторизован", 401);
        }

        String token = authToken.substring(7);

        if (!jwtToken.validateToken(token)) {
            throw new UserNotAuthorized("Неавторизован", 401);
        }

        log.info("Проверка пользователя на авторизацию: по Id пользователя: {}", userID);
        if (userID == null) {
            throw new UserNotAuthorized("Пользователь " + userID + " не авторизован.", 401);
        }

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