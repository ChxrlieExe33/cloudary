package com.cdcrane.cloudary.users.exceptions;

public class IdentityTakenException extends RuntimeException {
    public IdentityTakenException(String message) {
        super(message);
    }
}
