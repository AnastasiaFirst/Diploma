package com.example.cloud_service_diploma.exception;

public class ErrorDeleteFile extends RuntimeException {
    private final int id;

    public ErrorDeleteFile(String message, int id) {
        super(message);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
