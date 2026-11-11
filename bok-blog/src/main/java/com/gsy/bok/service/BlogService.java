package com.gsy.bok.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.gsy.bok.domain.Blog;

/**
 * 博客业务接口。继承 IService 后自带 save / updateById / removeById / list 等通用方法。
 */
public interface BlogService extends IService<Blog> {
}