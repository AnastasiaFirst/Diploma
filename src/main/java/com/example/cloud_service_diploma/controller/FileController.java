package com.example.cloud_service_diploma.controller;

import com.example.cloud_service_diploma.model.File;
import com.example.cloud_service_diploma.model.dto.FileDTO;
import com.example.cloud_service_diploma.model.dto.FileRenameRequest;
import com.example.cloud_service_diploma.security.JWTToken;
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

@RestController
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;
    private final JWTToken jwtToken;

    @Autowired
    public FileController(FileService fileService, JWTToken jwtToken) {
        this.fileService = fileService;
        this.jwtToken = jwtToken;
    }

    private Long getUserIdFromToken(String authToken) {
        if (authToken == null || !authToken.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неавторизован");
        }

        String token = authToken.substring(7);
        if (!jwtToken.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неавторизован");
        }

        return jwtToken.getAuthenticatedUser () != null ? jwtToken.getAuthenticatedUser ().getId() : null;
    }

    @PostMapping("/cloud/file")
    public ResponseEntity<Void> addFile(
            @RequestHeader(value = "Authorization", required = true) String authToken,
            @RequestParam(name = "filename") String fileName,
            @RequestPart(name = "file") MultipartFile file
    ) throws IOException {

        log.info("Запрос на загрузку файла на сервер: {}", fileName);

        Long userID = getUserIdFromToken(authToken);

        if (file.isEmpty()) {
        log.info("Файл для загрузки не выбран: {}", file.isEmpty());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл не выбран");
    }
        log.info("Файл для загрузки на сервер: {}", fileName);
        fileService.addFile(file, fileName, userID);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cloud/file")
    public ResponseEntity<byte[]> downloadFile(
            @RequestHeader(value = "Authorization", required = true) String authToken,
            @RequestParam("filename") String filename) throws IOException {

        Long userID = getUserIdFromToken(authToken);

        try {
            File file = fileService.getFile(filename, userID);

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
            @RequestHeader(value = "Authorization", required = true) String authToken,
            @RequestParam("filename") String filename,
            @RequestBody FileRenameRequest request) {

        Long userID = getUserIdFromToken(authToken);

        String newFileName = request.getNewFileName();

        if (newFileName == null || newFileName.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неправильный запрос на изменение имени файла");
        }

        log.info("Запрос на изменение имени файла: {} на {}", filename, newFileName);
        fileService.renameFile(filename, newFileName, userID);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cloud/file")
    public ResponseEntity<Void> deleteFile(
            @RequestHeader(value = "Authorization", required = true) String authToken,
            @RequestParam("filename") String filename) {

        Long userID = getUserIdFromToken(authToken);

        log.info("Запрос на удаление файла: {} для пользователя с ID {}", filename, userID);
        fileService.deleteFile(filename, userID);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cloud/list")
    public ResponseEntity<List<FileDTO>> listFiles(
            @RequestHeader(value = "Authorization", required = true) String authToken,
            @RequestParam(value = "limit", defaultValue = "10", required = false) int limit) {

        Long userID = getUserIdFromToken(authToken);

        log.info("Запрос на получение всех файлов для пользователя с ID {} с лимитом {}", userID, limit);
        List<FileDTO> files = fileService.getAllFiles(limit, userID);
        return ResponseEntity.ok(files);
    }
}
