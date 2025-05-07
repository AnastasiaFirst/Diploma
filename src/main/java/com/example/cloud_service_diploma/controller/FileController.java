package com.example.cloud_service_diploma.controller;

import com.example.cloud_service_diploma.model.File;
import com.example.cloud_service_diploma.model.dto.FileDTO;
import com.example.cloud_service_diploma.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/cloud/file")
    public ResponseEntity<File> addFile(
            @RequestHeader(value = "Authorization", required = true) String authToken,
            @RequestParam(name = "filename") String fileName,
            @RequestPart(name = "file") MultipartFile file
    ) throws IOException {

        log.info("Запрос на загрузку файла на сервер: {}", fileName);

        if (file.isEmpty()) {
        log.info("Файл для загрузки не выбран: {}", file.isEmpty());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл не выбран");
    }
        log.info("Файл для загрузки на сервер: {}", fileName);
        File savedFile = fileService.addFile(file, fileName, authToken);
        return new ResponseEntity<>(savedFile, HttpStatus.OK);
    }

    @GetMapping("/cloud/file")
    public ResponseEntity<byte[]> downloadFile(
            @RequestHeader(value = "auth-token", required = true) String authToken,
            @RequestParam("filename") String filename) throws IOException {

        if (!isValidToken(authToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неавторизован");
        }

        try {
            File file = fileService.getFile(filename, authToken);

            if (file == null || file.getFile() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл не найден или неверный запрос");
            }

            byte[] fileBytes = file.getFile();

            String encodedFileName = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("*=UTF-8''" + encodedFileName, StandardCharsets.UTF_8)
                    .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileBytes);
        } catch (IOException e) {

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при обработке запроса", e);
        }
    }

    @PutMapping("/cloud/file")
    public ResponseEntity<Void> editFileName(
            @RequestHeader(value = "auth-token", required = true) String authToken,
            @RequestParam("filename") String filename,
            @RequestBody(required = false) Map<String, String> requestBody) {

        if (!isValidToken(authToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неавторизован");
        }

        if (requestBody == null || !requestBody.containsKey("filename")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неправильный запрос на изменение имени файла");
        }

        String newFileName = requestBody.get("filename");

        try {
            FileDTO fileDto = FileDTO.build(newFileName);

            fileService.renameFile(filename, fileDto, authToken);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при переименовании файла", e);
        }
    }

    @DeleteMapping("/cloud/file")
    public ResponseEntity<Void> deleteFile(
            @RequestHeader(value = "auth-token", required = true) String authToken,
            @RequestParam("filename") String filename) {

        if (!isValidToken(authToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неавторизован");
        }

        if (filename == null || filename.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Имя файла не может быть пустым");
        }

        try {
            fileService.deleteFile(filename, authToken);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при удалении файла", e);
        }
    }

    @GetMapping("/cloud/list")
    public ResponseEntity<List<FileDTO>> listFiles(
            @RequestHeader(value = "auth-token", required = true) String authToken,
            @RequestParam(value = "limit", defaultValue = "10", required = false) int limit) {

        if (!isValidToken(authToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неавторизован");
        }

        if (limit <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Лимит должен быть положительным числом");
        }

        try {
            List<FileDTO> files = fileService.getAllFiles(limit, authToken);

            List<FileDTO> fileDTO = files.stream()
                    .map(file -> {
                        return new FileDTO(file.getFileName(), (int) file.getSize());
                    })
                    .toList();

            return new ResponseEntity<>(fileDTO, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isValidToken(String authToken) {
        return authToken != null && !authToken.isEmpty();
    }
}
