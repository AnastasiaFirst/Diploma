package com.example.cloud_service_diploma.model.dto;

import com.example.cloud_service_diploma.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Arrays;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileDTO {
    private UserEntity userId;
    private String fileName;
    private String hash;
    private byte[] file;
    private long size;
    private LocalDateTime fileUploadDate;

    public FileDTO(String fileName) {
        this.fileName = fileName;
    }

    public FileDTO(String fileName, long size) {
        this.fileName = fileName;
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return "FileDTO{" +
                "userId=" + userId +
                ", fileName='" + fileName + '\'' +
                ", hash='" + hash + '\'' +
                ", file=" + Arrays.toString(file) +
                ", size=" + size +
                ", fileUploadDate=" + fileUploadDate +
                '}';
    }

    public static FileDTO build(String fileName) {
        return new FileDTO(fileName);
    }

    public static FileDTO create(String fileName, long size) {
        return new FileDTO(fileName, size);
    }

}