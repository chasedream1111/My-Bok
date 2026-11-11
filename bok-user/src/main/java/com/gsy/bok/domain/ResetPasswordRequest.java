package com.gsy.bok.domain;

import lombok.Data;

/**
 * 重置密码请求体。
 * 说明：code 只是前端透传，真正的校验状态由 Redis 里的 reset:verified:{phone} 标记，
 * 避免"校验成功后又把验证码消费掉，导致重置时无法再验一次"的问题。
 */
@Data
public class ResetPasswordRequest {
    private String phone;
    private String code;
    private String password;
}
