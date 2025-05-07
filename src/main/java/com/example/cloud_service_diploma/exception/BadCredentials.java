package com.example.cloud_service_diploma.exception;

public class BadCredentials extends RuntimeException {

    private final int id;

    public BadCredentials(String message, int id) {
        super(message);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}