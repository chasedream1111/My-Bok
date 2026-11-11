package com.gsy.bok.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论点赞表 comment_like
 * (user_id, comment_id) 上有唯一索引，防止重复点赞
 */
@Data
@TableName("comment_like")
public class CommentLiked {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 点赞者id */
    private Long userId;

    /** 被点赞的评论id */
    private Long commentId;

    /** 创建时间（数据库默认值，插入时不传） */
    private LocalDateTime createTime;
}
