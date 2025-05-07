package com.example.cloud_service_diploma.exception;

public class SuccessAuthorization extends RuntimeException {
    private final int id;

    public SuccessAuthorization(String message, int id) {
        super(message);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}