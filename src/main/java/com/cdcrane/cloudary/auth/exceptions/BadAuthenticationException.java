package com.cdcrane.cloudary.auth.exceptions;

public class BadAuthenticationException extends RuntimeException {
    public BadAuthenticationException(String message) {
        super(message);
    }
}
