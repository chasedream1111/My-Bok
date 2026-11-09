package com.gsy.bok.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.gsy.bok.domain.CheckCode;
import com.gsy.bok.domain.User;
import com.gsy.bok.exception.CheckCodeDataException;
import com.gsy.bok.exception.DBException;
import com.gsy.bok.exception.RedisDataException;
import com.gsy.bok.exception.RepeatCodeException;
import com.gsy.bok.mapper.UserMapper;
import com.gsy.bok.service.UserService;
import com.gsy.bok.utils.Result;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.*;

/**
 * 用户业务实现。
 * 继承 ServiceImpl 后，IService 的通用方法都由 MyBatis-Plus 实现，无需自己写。
 */
@RequiredArgsConstructor
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;
    /**
     * 登录
     * @param user
     * @return
     */
    @Override
    public Result login(User user) {
        //isBlank方法只有在参数有具体值，比如"a"才返回true，其余都返回false
        if(user == null || StringUtils.isBlank(user.getAccount()) || StringUtils.isBlank(user.getPassword())){
            return Result.fail("登录信息请填写完整");
        }
        //查询数据库是否存在该用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount,user.getAccount()).select(User::getAccount).select(User::getPassword);
        User user_db = userMapper.selectOne(wrapper);
        //不存在，返回fail
        if(user_db == null){
            return Result.fail("未查询到账号为"+user.getAccount()+"的用户");
        }
        //存在就将该密码加密与查到的密码比较
        boolean res = passwordEncoder.matches(user.getPassword(), user_db.getPassword());
        return res?Result.success("登录成功"):Result.fail("密码错误，登陆失败，请重试");
    }

    /**
     * 注册
     * @param user
     * @return
     */
    @Override
    public Result sign(User user) {
        if(user == null || user.getGender() == null ||
                StringUtils.isBlank(user.getAccount()) || StringUtils.isBlank(user.getPassword()) ||
                StringUtils.isBlank(user.getAddress()) || StringUtils.isBlank(user.getEmail()) ||
                StringUtils.isBlank(user.getImageUrl()) || StringUtils.isBlank(user.getNickname()) ||
                StringUtils.isBlank(user.getPhone())){
            return Result.fail("注册信息不完整，请重新填写");
        }
        // 查询该手机号所对应用户是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone,user.getPhone());
        User user_db = userMapper.selectOne(wrapper);
        // 存在就返回fail
        if(user_db != null){
            return Result.fail("该用户已存在");
        }
        // 不存在就生成唯一账号，加密密码
        String account = generate_global_account();
        user.setAccount(account);
        String encode_password = passwordEncoder.encode(user.getPassword());
        user.setPassword(encode_password);
        // 插入数据库 有全局异常，失败会被捕获
        int insert = userMapper.insert(user);
        if(insert == 0){
            throw new DBException("数据库操作异常");
        }
        return Result.success("注册成功");
    }

    /**
     * 校验验证码
     * @return
     */
    @Override
    public Result check(CheckCode checkCode) {
        if(checkCode == null || StringUtils.isBlank(checkCode.getPhone()) || StringUtils.isBlank(checkCode.getCode())){
            throw new CheckCodeDataException("注册时手机号发送验证码校验异常");
        }
        // 查询redis中存储的短期验证码，进行比较
        String key = "register:"+"code:" + checkCode.getPhone();
        String res = stringRedisTemplate.opsForValue().get(key);
        if (res == null) {
            throw new RedisDataException("短期验证码不存在");
        }
        return res.equals(checkCode.getCode())?Result.success("验证码校验通过"):Result.fail("验证码错误");
    }

    private String generate_global_account(){
        /**
         * 采用10位毫秒时间戳 + redis自增实现全局唯一账号，账号总长为13位
         *
         * 2001‑09‑09 01:46:40.000**，从这一刻开始，毫秒时间戳正式变成 10 位
         * 2286‑11‑20 17:46:39.999**，过了这个点就变成 11 位
         */
        // 统一用 UTC
        ZoneId zoneId = ZoneId.of("UTC");
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        // 秒时间戳（UTC）
        long account_front = now.toEpochSecond();
        // UTC 的日期，和上面时间戳同源
        LocalDate n = now.toLocalDate();
        String key_redis = "account:redis_back:" + n.getYear() + ":" + n.getMonth() + ":" + n.getDayOfMonth();
        Long account_back = stringRedisTemplate.opsForValue().increment(key_redis);
        if(account_back != null && account_back == 1){
            stringRedisTemplate.expire(key_redis, Duration.ofHours(25));
        }
        return String.valueOf(account_front * 1000 + account_back);
    }
}
