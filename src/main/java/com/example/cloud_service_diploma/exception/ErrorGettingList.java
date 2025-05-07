package com.example.cloud_service_diploma.exception;

public class ErrorGettingList extends RuntimeException {
    private final int id;

    public ErrorGettingList(String message, int id) {
        super(message);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
