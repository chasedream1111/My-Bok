package com.gsy.bok.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.gsy.bok.domain.Blog;
import com.gsy.bok.mapper.BlogMapper;
import com.gsy.bok.service.BlogService;
import org.springframework.stereotype.Service;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {
}