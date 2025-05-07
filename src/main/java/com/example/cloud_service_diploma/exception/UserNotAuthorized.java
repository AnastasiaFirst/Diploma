package com.example.cloud_service_diploma.exception;

public class UserNotAuthorized extends RuntimeException {
    private final int id;

    public UserNotAuthorized(String message, int id) {
        super(message);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
