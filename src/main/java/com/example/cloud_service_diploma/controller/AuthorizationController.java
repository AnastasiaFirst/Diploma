package com.example.cloud_service_diploma.controller;


import com.example.cloud_service_diploma.config.AuthenticationConfigConstants;
import com.example.cloud_service_diploma.entity.UserEntity;
import com.example.cloud_service_diploma.exception.BadCredentials;
import com.example.cloud_service_diploma.model.dto.UserDto;
import com.example.cloud_service_diploma.model.Login;
import com.example.cloud_service_diploma.security.JWTToken;
import com.example.cloud_service_diploma.service.AuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;



@RestController
public class AuthorizationController {
    private static final Logger log = LoggerFactory.getLogger(AuthorizationController.class);

    private final AuthorizationService authorizationService;
    private final JWTToken jwtToken;

    public AuthorizationController(AuthorizationService authorizationService, JWTToken jwtToken) {
        this.authorizationService = authorizationService;
        this.jwtToken = jwtToken;
    }

    @PostMapping("/cloud/login")
    public ResponseEntity<Login> authorizationLogin(@RequestBody UserDto userDto) throws NoSuchAlgorithmException {
        log.info("Пользователь пытается войти в систему: {}", userDto);
        UserEntity userEntity = authorizationService.authorizationLogin(userDto);

        if (!userDto.getPassword().equals(userEntity.getPassword())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String token = jwtToken.generateToken(userEntity);
        log.info("Пользователь: {} успешно вошел в систему. Auth-token: {}", userDto.getLogin(), token);
        return new ResponseEntity<>(new Login(token), HttpStatus.OK);
    }

    @PostMapping("/cloud/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = AuthenticationConfigConstants.AUTH_TOKEN) String authToken,
                                       HttpServletRequest request, HttpServletResponse response) {
        try {
            if (!jwtToken.validateToken(authToken)) {
                throw new BadCredentials("Неверный или истекший токен", 400);
            }
            jwtToken.removeToken(authToken);
            log.info("Пользователь успешно вышел из системы. Auth-token: {}", authToken);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (BadCredentials e) {
            log.error("Ошибка выхода: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Ошибка: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
