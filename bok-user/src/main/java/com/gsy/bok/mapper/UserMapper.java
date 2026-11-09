package com.gsy.bok.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gsy.bok.domain.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 Mapper，继承 BaseMapper 后即拥有单表增删改查能力。
 * 复杂 SQL 可写在 resources/mapper/UserMapper.xml 中（application.yml 已配置 mapper-locations）。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
