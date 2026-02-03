package com.cdcrane.cloudary.files.exceptions;

public class S3UploadFailedException extends RuntimeException{
    public S3UploadFailedException(String message) {
        super(message);
    }
}
