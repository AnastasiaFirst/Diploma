package com.example.cloud_service_diploma.service;

import com.example.cloud_service_diploma.model.dto.UserDto;
import com.example.cloud_service_diploma.entity.UserEntity;
import com.example.cloud_service_diploma.exception.BadCredentials;
import com.example.cloud_service_diploma.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;


@Service
public class AuthorizationService {
    private static final Logger log = LoggerFactory.getLogger(AuthorizationService.class);

    @Autowired
    private final UserRepository userRepository;

    public AuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity authorizationLogin(UserDto userDto) throws NoSuchAlgorithmException {
        log.info("Поиск пользователя в базе данных по логину: {}", userDto.getLogin());
        UserEntity userEntity = userRepository.findUserByLogin(userDto.getLogin()).orElseThrow(() ->
                new BadCredentials("Пользователь не найден", 400));
        return userEntity;
    }
}
