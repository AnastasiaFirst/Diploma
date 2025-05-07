package com.example.cloud_service_diploma.security;

import com.example.cloud_service_diploma.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JWTToken jwtToken;

    public JwtService(JWTToken jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtToken.getSecretKey().getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }


    public String generateToken(UserEntity userEntity) {
        return jwtToken.generateToken(userEntity);
    }

    public boolean validateToken(String token) {
        return jwtToken.validateToken(token);
    }

    public void removeToken(String token) {
        jwtToken.removeToken(token);
    }
}