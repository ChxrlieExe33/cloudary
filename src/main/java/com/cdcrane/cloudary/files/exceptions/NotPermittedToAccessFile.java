package com.cdcrane.cloudary.files.exceptions;

public class NotPermittedToAccessFile extends RuntimeException{
    public NotPermittedToAccessFile(String message) {
        super(message);
    }
}
