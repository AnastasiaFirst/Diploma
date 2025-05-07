package com.example.cloud_service_diploma.security;

import com.example.cloud_service_diploma.config.AuthenticationConfigConstants;
import com.example.cloud_service_diploma.entity.UserEntity;
import com.example.cloud_service_diploma.repositories.UserRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ConfigurationProperties(prefix = "jwt")
@Component
public class JWTToken {
    private static final Logger log = LoggerFactory.getLogger(JWTToken.class);
    private String secretKey;
    private UserEntity userEntity;
    private final Set<String> activeTokens = ConcurrentHashMap.newKeySet();

    @Autowired
    private UserRepository userRepository;

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    private byte[] hexStringToByteArray(String s) {

        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public UserEntity getAuthenticatedUser() {
        return userEntity;
    }

    public String generateToken(@NonNull UserEntity userEntity) throws IllegalArgumentException {
        log.info("Generating token for user: {}", userEntity.getLogin());
        this.userEntity = userEntity;
        Date now = new Date();
        Date exp = new Date(System.currentTimeMillis() + 3600000);

        byte[] keyBytes = hexStringToByteArray(secretKey);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        log.info("User  ID being set in token: {}", userEntity.getId());
        log.info("Генерация токена для пользователя: {}", userEntity.getLogin());

        String token = Jwts.builder()
                .setId(String.valueOf(userEntity.getId()))
                .setSubject(userEntity.getLogin())
                .setIssuedAt(now)
                .setNotBefore(now)
                .setExpiration(exp)
                .signWith(key)
                .compact();
        log.info("Auth-token {} сформирован", token);
        activeTokens.add(token);
        log.info("Token added to active tokens: {}", token);
        return token;
    }

    public boolean validateToken(String token) {
        try {

            byte[] keyBytes = hexStringToByteArray(secretKey);
            Key key = Keys.hmacShaKeyFor(keyBytes);

            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            log.info("Token is valid!");
            return true;
        } catch (JwtException | IllegalArgumentException e) {

            log.error("Token validation failed");
            return false;
        }
    }

//    public boolean isTokenActive(String token) {
//        return activeTokens.contains(token);
//    }

    public void removeToken(String token) {
        activeTokens.remove(token.substring(AuthenticationConfigConstants.TOKEN_PREFIX.length()));
        log.info("Token removed from active tokens: {}", token);
    }
}