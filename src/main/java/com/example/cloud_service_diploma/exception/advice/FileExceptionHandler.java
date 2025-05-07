package com.example.cloud_service_diploma.exception.advice;

import com.example.cloud_service_diploma.exception.*;
import com.example.cloud_service_diploma.model.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class FileExceptionHandler {
    @ExceptionHandler(UserNotAuthorized.class)
    public ResponseEntity<Error> handleUserNotAuthorizedException(UserNotAuthorized ex) {
        Error error = new Error("User not authorized" + ex.getMessage(), 401);
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SuccessUpload.class)
    public ResponseEntity<Error> handleSuccessUploadException(SuccessUpload ex) {
        Error error = new Error("Success upload" + ex.getMessage(), 200);
        return new ResponseEntity<>(error, HttpStatus.OK);
    }

    @ExceptionHandler(ErrorInputData.class)
    public ResponseEntity<Error> handleErrorInputDataException(ErrorInputData ex) {
        Error error = new Error("Error input data" + ex.getMessage(), 400);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ErrorUploadFile.class)
    public ResponseEntity<Error> handleErrorUploadFileException(ErrorUploadFile ex) {
        Error error = new Error("Error upload file" + ex.getMessage(), 500);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SuccessDeleted.class)
    public ResponseEntity<Error> handleSuccessDeletedException(SuccessDeleted ex) {
        Error error = new Error("Success deleted" + ex.getMessage(), 200);
        return new ResponseEntity<>(error, HttpStatus.OK);
    }

    @ExceptionHandler(ErrorDeleteFile.class)
    public ResponseEntity<Error> handleErrorDeleteFileException(ErrorDeleteFile ex) {
        Error error = new Error("Error delete file" + ex.getMessage(), 500);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ErrorGettingList.class)
    public ResponseEntity<Error> handleErrorGettingListException(ErrorGettingList ex) {
        Error error = new Error("Error getting file list" + ex.getMessage(), 500);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
