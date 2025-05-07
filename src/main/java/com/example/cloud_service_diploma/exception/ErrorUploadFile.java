package com.example.cloud_service_diploma.exception;

public class ErrorUploadFile extends RuntimeException {
    private final int id;

    public ErrorUploadFile(String message, int id) {
        super(message);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
