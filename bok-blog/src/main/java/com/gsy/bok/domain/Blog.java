package com.gsy.bok.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 博客表 blog
 */
@Data
@TableName("blog")
public class Blog {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作者id */
    private Long userId;

    /** 标题 */
    private String title;

    /** 正文 */
    private String content;

    /** 封面图，可空 */
    private String imageUrl;

    /** 类型：1技术分享 2生活随笔 3学习笔记 4情感感悟 5其他 */
    private Integer type;

    /** 创建时间（数据库默认值，插入时不传） */
    private LocalDateTime createTime;

    /** 更新时间（数据库自动维护，插入/更新时不传） */
    private LocalDateTime updateTime;

    /** 逻辑删除：0正常 1已删除 */
    @TableLogic
    private Integer deleted;
}
