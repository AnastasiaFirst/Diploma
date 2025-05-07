package com.example.cloud_service_diploma.exception;

public class SuccessLogout extends RuntimeException {
    private final int id;

    public SuccessLogout(String message, int id) {
        super(message);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}