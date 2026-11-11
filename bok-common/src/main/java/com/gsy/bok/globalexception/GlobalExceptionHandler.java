package com.gsy.bok.globalexception;

import com.gsy.bok.exception.*;
import com.gsy.bok.utils.Result;
import com.gsy.bok.utils.Results;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：每个处理器都返回带 HTTP 状态码的响应，
 * 不再出现"HTTP 200 + body.code=500"这种无法被网关/监控识别的情况。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 数据库操作异常：服务端问题，500 */
    @ExceptionHandler(DBException.class)
    public ResponseEntity<Result<?>> db(DBException e) {
        e.printStackTrace();
        return Results.toResponse(Result.fail(500, e.getMessage()));
    }

    /** 验证码参数缺失：客户端传参问题，400 */
    @ExceptionHandler(CheckCodeDataException.class)
    public ResponseEntity<Result<?>> ccd(CheckCodeDataException e) {
        e.printStackTrace();
        return Results.toResponse(Result.fail(400, e.getMessage()));
    }

    /** 验证码错误次数过多：429，提示调用方稍后再试 */
    @ExceptionHandler(RepeatCodeException.class)
    public ResponseEntity<Result<?>> rc(RepeatCodeException e) {
        e.printStackTrace();
        return Results.toResponse(Result.fail(429, e.getMessage()));
    }

    /**
     * 修正：参数类型原来是 RepeatCodeException，与实际处理的 RedisDataException 不匹配，
     * 导致 Spring 找不到可用的参数解析器，直接返回默认的 500 错误页。
     */
    @ExceptionHandler(RedisDataException.class)
    public ResponseEntity<Result<?>> rc2(RedisDataException e) {
        e.printStackTrace();
        return Results.toResponse(Result.fail(400, e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Result<?>> illState(IllegalStateException e) {
        e.printStackTrace();
        return Results.toResponse(Result.fail(400, e.getMessage()));
    }

    @ExceptionHandler(TokenExpirationException.class)
    public ResponseEntity<Result<?>> tokenExp(TokenExpirationException e) {
        e.printStackTrace();
        return Results.toResponse(Result.fail(400, e.getMessage()));
    }

    @ExceptionHandler(JwtParseProcessException.class)
    public ResponseEntity<Result<?>> jwtParse(JwtParseProcessException e) {
        e.printStackTrace();
        return Results.toResponse(Result.fail(400, e.getMessage()));
    }

}
