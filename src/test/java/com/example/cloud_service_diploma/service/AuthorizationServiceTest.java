package com.example.cloud_service_diploma.service;

import com.example.cloud_service_diploma.entity.UserEntity;
import com.example.cloud_service_diploma.enumiration.Role;
import com.example.cloud_service_diploma.exception.BadCredentials;
import com.example.cloud_service_diploma.exception.SuccessLogout;
import com.example.cloud_service_diploma.model.dto.UserDto;
import com.example.cloud_service_diploma.repositories.UserRepository;
import com.example.cloud_service_diploma.security.JWTToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorizationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private JWTToken jwtToken;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAuthorizationLoginUserNotFound() {
        UserDto userDto = new UserDto("testUser ", "password", Set.of(Role.ROLE_ADMIN));

        when(userRepository.findUserByLogin(userDto.getLogin())).thenReturn(Optional.empty());

        BadCredentials exception = assertThrows(BadCredentials.class, () -> {
            authorizationService.authorizationLogin(userDto);
        });

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void testLogoutInvalidToken() {
        String authToken = "invalidToken";
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(jwtToken.validateToken(authToken)).thenReturn(false);

        BadCredentials exception = assertThrows(BadCredentials.class, () -> {
            authorizationService.logout(authToken, request, response);
        });

        assertEquals("Неверный или истекший токен", exception.getMessage());
    }

}