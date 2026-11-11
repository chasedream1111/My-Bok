package com.gsy.bok.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.gsy.bok.domain.CheckCode;
import com.gsy.bok.domain.User;
import com.gsy.bok.utils.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 用户业务接口。
 * 继承 IService 后即拥有 save / getById / updateById / removeById / list 等通用方法。
 * 注意：MyBatis-Plus 3.5.18 起 IService 位于 com.baomidou.mybatisplus.spring.service。
 */
public interface UserService extends IService<User> {
    Result login(User user);

    Result sign(User user);

    Result check(CheckCode checkCode);

    void generate_code(String phone, HttpServletResponse response) throws IOException;

    /** 找回密码用的图形验证码（Redis key 与注册区分开） */
    void generate_reset_code(String phone, HttpServletResponse response) throws IOException;

    /** 找回密码用的验证码校验 */
    Result check_reset(CheckCode checkCode);

    Result upload(MultipartFile file);

    Result forget(User user);

    Result user_info();

    Result get_info();

    Result edit_info(User user);

    Result logout();
}
