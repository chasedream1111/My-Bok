package com.gsy.bok.exception;

public class JwtParseProcessException extends RuntimeException{

    public JwtParseProcessException() {
    }

    public JwtParseProcessException(String message) {
        super(message);
    }
}
