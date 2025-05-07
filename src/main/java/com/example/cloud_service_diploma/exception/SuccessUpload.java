package com.example.cloud_service_diploma.exception;

public class SuccessUpload extends RuntimeException {
    private final int id;

    public SuccessUpload(String message, int id) {
        super(message);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
