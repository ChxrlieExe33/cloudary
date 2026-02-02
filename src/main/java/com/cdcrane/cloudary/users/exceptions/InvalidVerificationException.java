package com.cdcrane.cloudary.users.exceptions;

public class InvalidVerificationException extends RuntimeException{
    public InvalidVerificationException(String message) {
        super(message);
    }
}
