package com.gsy.bok.exception;

public class TokenExpirationException extends RuntimeException{

    public TokenExpirationException() {
    }

    public TokenExpirationException(String message) {
        super(message);
    }
}
