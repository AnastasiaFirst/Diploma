package com.example.cloud_service_diploma.security;

import com.example.cloud_service_diploma.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    private final JWTToken jwtToken;
    private final SecretKey signingKey;

    public JwtService(JWTToken jwtToken) {
        this.jwtToken = jwtToken;
        this.signingKey = jwtToken.getSgningKey();
    }

    public String extractUsername(String token) {
        JwtParser parser = Jwts.parser()
                .setSigningKey(signingKey)
                .build();

        Claims claims = parser.parseClaimsJws(token).getBody();

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
