package com.example.cloud_service_diploma.exception;

public class ErrorInputData extends RuntimeException {
    private final int id;

    public ErrorInputData(String message, int id) {
        super(message);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
