package com.cdcrane.cloudary.auth.exceptions;

public class NotPermittedToRevokeAuthException extends RuntimeException {
    public NotPermittedToRevokeAuthException(String message) {
        super(message);
    }
}
