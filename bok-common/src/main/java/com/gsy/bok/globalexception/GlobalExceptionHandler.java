package com.gsy.bok.globalexception;

import com.gsy.bok.exception.CheckCodeDataException;
import com.gsy.bok.exception.DBException;
import com.gsy.bok.exception.RedisDataException;
import com.gsy.bok.exception.RepeatCodeException;
import com.gsy.bok.utils.Result;
import com.gsy.bok.utils.Results;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
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

    /**
     * 唯一索引冲突：手机号/账号已存在时，MyBatis-Spring 会把
     * java.sql.SQLIntegrityConstraintViolationException 翻译成 DuplicateKeyException。
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<?>> duplicateKey(DuplicateKeyException e) {
        e.printStackTrace();
        return Results.toResponse(Result.fail(409, "该手机号或账号已被注册，请更换后重试"));
    }

    /**
     * 其它数据完整性异常（字段超长、非空约束等）。
     * DuplicateKeyException 是它的子类，会优先命中上面更具体的处理器。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<?>> dataIntegrity(DataIntegrityViolationException e) {
        e.printStackTrace();
        return Results.toResponse(Result.fail(400, "提交的数据不符合要求，请检查后重试"));
    }
}
