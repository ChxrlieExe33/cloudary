package com.cdcrane.cloudary.auth.exceptions;

public class BadJwtException extends RuntimeException {

    public BadJwtException(String message) {
        super(message);
    }
}
