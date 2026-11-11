package com.gsy.bok.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 博客点赞表 blog_like
 * (user_id, blog_id) 上有唯一索引，防止重复点赞
 */
@Data
@TableName("blog_like")
public class BlogLiked {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 点赞者id */
    private Long userId;

    /** 被点赞的博客id */
    private Long blogId;

    /** 创建时间（数据库默认值，插入时不传） */
    private LocalDateTime createTime;
}
