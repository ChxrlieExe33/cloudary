package com.cdcrane.cloudary.files.exceptions;

public class UserNotInPermittedListException extends RuntimeException {
    public UserNotInPermittedListException(String message) {
        super(message);
    }
}
