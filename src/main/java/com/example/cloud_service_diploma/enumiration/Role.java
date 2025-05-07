package com.example.cloud_service_diploma.enumiration;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
public enum Role implements GrantedAuthority {

    ROLE_ADMIN("ROLE_ADMIN"),
    ROLE_USER("ROLE_USER");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    @Override
    public String getAuthority() {
        return value;
    }
}