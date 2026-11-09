package com.gsy.bok.exception;

public class RedisDataException extends RuntimeException{

    public RedisDataException() {
    }

    public RedisDataException(String message) {
        super(message);
    }
}
