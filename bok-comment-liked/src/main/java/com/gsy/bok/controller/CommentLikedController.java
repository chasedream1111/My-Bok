package com.gsy.bok.controller;

import com.gsy.bok.service.CommentLikedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论点赞接口。
 * <p>
 * 骨架已就绪（注入的 service 可以直接用），具体接口由你自己实现。
 */
@RestController
@RequestMapping("/blog-comment-liked")
@RequiredArgsConstructor
public class CommentLikedController {

    private final CommentLikedService commentLikedService;
}