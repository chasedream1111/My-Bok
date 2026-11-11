package com.gsy.bok.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.gsy.bok.domain.CommentLiked;
import com.gsy.bok.mapper.CommentLikedMapper;
import com.gsy.bok.service.CommentLikedService;
import org.springframework.stereotype.Service;

@Service
public class CommentLikedServiceImpl extends ServiceImpl<CommentLikedMapper, CommentLiked> implements CommentLikedService {
}