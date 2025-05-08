package com.example.cloud_service_diploma.service;

import com.example.cloud_service_diploma.entity.UserEntity;
import com.example.cloud_service_diploma.exception.BadCredentials;
import com.example.cloud_service_diploma.model.dto.UserDto;
import com.example.cloud_service_diploma.repositories.UserRepository;
import com.example.cloud_service_diploma.security.JWTToken;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorizationServiceTest {
    @InjectMocks
    private AuthorizationService authorizationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JWTToken jwtToken;

    @Mock
    private HttpServletRequest request;

    private UserEntity userEntity;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userEntity = new UserEntity();
        userEntity.setLogin("testUser ");
        userEntity.setPassword("password123");
    }

    @Test
    public void testAuthorizationLoginSuccess() throws NoSuchAlgorithmException {
        UserDto userDto = new UserDto();
        userDto.setLogin("testUser ");
        userDto.setPassword("password123");

        when(userRepository.findUserByLogin("testUser ")).thenReturn(Optional.of(userEntity));
        when(jwtToken.generateToken(userEntity)).thenReturn("mockedToken");

        UserEntity resultUserEntity = authorizationService.authorizationLogin(userDto);

        assertNotNull(resultUserEntity);
        assertEquals("testUser ", resultUserEntity.getLogin());

        String token = jwtToken.generateToken(resultUserEntity);
        assertEquals("mockedToken", token);

        verify(userRepository).findUserByLogin("testUser ");
        verify(jwtToken).generateToken(userEntity);
    }

    @Test
    public void testAuthorizationLoginUserNotFound() {
        UserDto userDto = new UserDto();
        userDto.setLogin("unknownUser ");
        userDto.setPassword("password123");

        when(userRepository.findUserByLogin("unknownUser ")).thenReturn(Optional.empty());

        BadCredentials exception = assertThrows(BadCredentials.class, () -> {
            authorizationService.authorizationLogin(userDto);
        });

        assertEquals("Пользователь не найден", exception.getMessage());
    }
}
