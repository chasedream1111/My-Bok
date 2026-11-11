package com.gsy.bok.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gsy.bok.domain.BlogComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlogCommentMapper extends BaseMapper<BlogComment> {
}