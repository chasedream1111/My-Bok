package com.gsy.bok.controller;

import com.gsy.bok.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 博客接口。
 * <p>
 * 骨架已就绪（注入的 service 可以直接用），具体接口由你自己实现。
 */
@RestController
@RequestMapping("/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    /**
     * 查询博客详情
     */

    /**
     * 新增博客
     */

    /**
     * 删除博客
     */

    /**
     * 查询博客列表
     */
}