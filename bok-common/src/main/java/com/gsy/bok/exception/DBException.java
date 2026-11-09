package com.gsy.bok.exception;

public class DBException extends RuntimeException{

    public DBException() {
    }

    public DBException(String message) {
        super(message);
    }

}
