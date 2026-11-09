package com.gsy.bok.exception;

public class RepeatCodeException extends RuntimeException{

    public RepeatCodeException() {
    }

    public RepeatCodeException(String message) {
        super(message);
    }
}
