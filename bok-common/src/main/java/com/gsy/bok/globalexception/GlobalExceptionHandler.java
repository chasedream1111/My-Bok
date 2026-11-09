package com.gsy.bok.globalexception;

import com.gsy.bok.exception.CheckCodeDataException;
import com.gsy.bok.exception.DBException;
import com.gsy.bok.exception.RedisDataException;
import com.gsy.bok.exception.RepeatCodeException;
import com.gsy.bok.utils.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(DBException.class)
    public Result db(DBException e){
        e.printStackTrace();
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(CheckCodeDataException.class)
    public Result ccd(CheckCodeDataException e){
        e.printStackTrace();
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(RepeatCodeException.class)
    public Result rc(RepeatCodeException e){
        e.printStackTrace();
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(RedisDataException.class)
    public Result rc2(RepeatCodeException e){
        e.printStackTrace();
        return Result.fail(e.getMessage());
    }
}
