package com.gsy.bok.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 博客评论表 blog_comment
 * <p>
 * parent_id 为 null 表示直接评论博客；有值表示回复某条评论（建议始终指向顶层评论，保持两级结构）。
 * last_user_id 是被回复者的 id，写入时由 parent 评论推导，仅用于展示"回复@某人"。
 */
@Data
@TableName("blog_comment")
public class BlogComment {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 被评论的博客id */
    private Long blogId;

    /** 评论者id */
    private Long userId;

    /** 被回复的评论id，null=直接评论博客 */
    private Long parentId;

    /** 被回复者id，null=直接评论博客 */
    private Long lastUserId;

    /** 评论内容 */
    private String content;

    /** 创建时间（数据库默认值，插入时不传） */
    private LocalDateTime createTime;

    /** 逻辑删除：0正常 1已删除 */
    @TableLogic
    private Integer deleted;
}
