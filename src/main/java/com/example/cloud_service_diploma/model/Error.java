package com.example.cloud_service_diploma.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
public class Error {
    private String message;
    private int id;

    public Error(String message, int id) {
        this.message = message;
        this.id = id;
    }
}