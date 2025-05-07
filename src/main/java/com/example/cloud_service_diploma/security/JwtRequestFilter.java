package com.example.cloud_service_diploma.security;

import com.example.cloud_service_diploma.entity.UserEntity;
import com.example.cloud_service_diploma.enumiration.Role;
import com.example.cloud_service_diploma.repositories.UserRepository;
import com.example.cloud_service_diploma.service.FileService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {


    private static final Logger log = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JWTToken jwtToken;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        if (requestPath.startsWith("/cloud/login")) {
            chain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt;

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            log.info("Header: {} = {}", headerName, headerValue);
        }

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);

            boolean isValid = jwtService.validateToken(jwt);

            if (isValid) {
                username = jwtService.extractUsername(jwt);
                log.info("Username from JWT: {}", username);
                log.info("Token is active");
            } else {
                log.warn("Invalid JWT Token: isValid=" + isValid);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token");
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserEntity userEntity = userRepository.findUserByLogin(username).orElseThrow(()
                    -> new UsernameNotFoundException("User not found"));

            if (userEntity.getRole().isEmpty()) {

                userEntity.setRole(Collections.singleton(Role.ROLE_USER));
            }

            userEntity.getRole().size();

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userEntity, null, userEntity.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            log.info("Authentication for user: {}", userEntity.getUsername());
        }

        chain.doFilter(request, response);
    }
}