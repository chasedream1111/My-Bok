package com.gsy.bok.globalexception;

import com.gsy.bok.utils.Result;
import com.gsy.bok.utils.Results;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 数据库相关的异常处理。
 * <p>
 * 放在 bok-user 而不是 bok-common：这些异常来自 spring-tx，
 * 网关是响应式的、没有 JDBC，如果放在公共模块，网关启动时会因
 * NoClassDefFoundError: DuplicateKeyException 直接失败。
 */
@RestControllerAdvice
public class DataAccessExceptionHandler {

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
