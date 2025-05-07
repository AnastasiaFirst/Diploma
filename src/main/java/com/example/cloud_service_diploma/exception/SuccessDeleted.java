package com.example.cloud_service_diploma.exception;

public class SuccessDeleted extends RuntimeException {
    private final int id;

    public SuccessDeleted(String message, int id) {
        super(message);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
