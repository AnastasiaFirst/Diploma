package com.example.cloud_service_diploma.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Login {

    @JsonProperty("auth-token")
    private String token;

    public String getToken() {
        return token;
    }

    public Login(String token) {
        this.token = token;
    }
}