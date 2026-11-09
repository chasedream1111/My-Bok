package com.gsy.bok.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("users")
public class User {
    private Long id;
    private String account;
    private String nickname;
    private Integer gender;
    private Integer age;
    private String email;
    private String phone;
    private String password;
    private String address;
    private String imageUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
