package com.gsy.bok.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.gsy.bok.domain.CheckCode;
import com.gsy.bok.domain.User;
import com.gsy.bok.utils.Result;

/**
 * 用户业务接口。
 * 继承 IService 后即拥有 save / getById / updateById / removeById / list 等通用方法。
 * 注意：MyBatis-Plus 3.5.18 起 IService 位于 com.baomidou.mybatisplus.spring.service。
 */
public interface UserService extends IService<User> {
    Result login(User user);

    Result sign(User user);

    Result check(CheckCode checkCode);
}
