package com.example.cloud_service_diploma.model;

import lombok.*;

@Getter
@Setter
@Builder
public class File {

    public File() {
    }

    public File(String hash, byte[] file) {
        this.hash = hash;
        this.file = file;
    }

    private String hash;
    private byte[] file;

    public byte[] getFile() {
        return file;
    }

    public String getHash() {
        return hash;
    }

    public static File build(String hash, byte[] file) {
        return new File(hash, file);
    }
}