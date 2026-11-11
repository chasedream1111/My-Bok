package com.gsy.bok.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.gsy.bok.domain.BlogComment;
import com.gsy.bok.mapper.BlogCommentMapper;
import com.gsy.bok.service.BlogCommentService;
import org.springframework.stereotype.Service;

@Service
public class BlogCommentServiceImpl extends ServiceImpl<BlogCommentMapper, BlogComment> implements BlogCommentService {
}