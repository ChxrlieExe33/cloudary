package com.cdcrane.cloudary.files.exceptions;

public class UploadedFileNotFoundException extends RuntimeException{
    public UploadedFileNotFoundException(String message) {
        super(message);
    }
}
