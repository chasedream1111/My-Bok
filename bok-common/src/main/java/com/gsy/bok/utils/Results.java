package com.gsy.bok.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 把 Result 里的业务 code 映射成真正的 HTTP 状态码，
 * 让网关、Nginx、监控、前端拦截器都能按状态码判断成功失败。
 */
public final class Results {

    private Results() {
    }

    public static ResponseEntity<Result<?>> toResponse(Result<?> result) {
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.fail("服务返回为空"));
        }
        Integer code = result.getCode();
        HttpStatus status = switch (code == null ? 500 : code) {
            case 200 -> HttpStatus.OK;
            case 400 -> HttpStatus.BAD_REQUEST;
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            case 429 -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status).body(result);
    }
}
