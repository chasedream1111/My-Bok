package com.gsy.bok.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.gsy.bok.domain.BlogLiked;
import com.gsy.bok.mapper.BlogLikedMapper;
import com.gsy.bok.service.BlogLikedService;
import org.springframework.stereotype.Service;

@Service
public class BlogLikedServiceImpl extends ServiceImpl<BlogLikedMapper, BlogLiked> implements BlogLikedService {
}