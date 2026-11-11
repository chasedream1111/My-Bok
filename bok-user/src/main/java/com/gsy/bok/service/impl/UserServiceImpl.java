package com.gsy.bok.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.gsy.bok.domain.CheckCode;
import com.gsy.bok.domain.LoginInfo;
import com.gsy.bok.domain.User;
import com.gsy.bok.exception.CheckCodeDataException;
import com.gsy.bok.exception.DBException;
import com.gsy.bok.exception.RedisDataException;
import com.gsy.bok.exception.RepeatCodeException;
import com.gsy.bok.mapper.UserMapper;
import com.gsy.bok.service.UserService;
import com.gsy.bok.utils.JwtUtil;
import com.gsy.bok.utils.OssUtil;
import com.gsy.bok.utils.Result;
import com.gsy.bok.utils.UserContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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
    private final OssUtil ossUtil;
    @Value("${jwt.login.expiration:30}")
    private long expiration;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /** 登录后token有效期的redis key*/
    private static final String TOKEN_KEY = "login:token:";
    /** 同一个验证码允许的最大错误次数，超过就作废 */
    private static final long MAX_CODE_FAIL = 5;

    /** 注册：验证码校验通过后的标记，注册接口凭它放行 */
    private static final String REGISTER_VERIFIED_KEY = "register:verified:";
    /** 找回密码用的一套独立 key，避免和注册的验证码互相覆盖 */
    private static final String RESET_CODE_KEY = "reset:code:";
    private static final String RESET_FAIL_KEY = "reset:code:fail:";
    /** 校验通过后的标记，10 分钟内可执行重置 */
    private static final String RESET_VERIFIED_KEY = "reset:verified:";

    /**
     * 登录
     * @param user
     * @return
     */
    @Override
    public Result login(User user) {
        //isBlank方法只有在参数有具体值，比如"a"才返回true，其余都返回false
        if(user == null || StringUtils.isBlank(user.getAccount()) || StringUtils.isBlank(user.getPassword())){
            return Result.fail(400, "登录信息请填写完整");
        }
        //查询数据库是否存在该用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 登录成功要签发 token，需要 id，所以把 id 一起查出来
        wrapper.eq(User::getAccount,user.getAccount())
                .select(User::getId, User::getAccount, User::getPassword);
        User user_db = userMapper.selectOne(wrapper);
        // 账号不存在和密码错误必须返回同一句提示，否则等于给攻击者提供账号枚举能力
        if(user_db == null || !passwordEncoder.matches(user.getPassword(), user_db.getPassword())){
            return Result.fail(401, "账号或密码错误");
        }
        // 登录成功签发 JWT，前端后续请求把它放在 Authorization 头里
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user_db.getId());
        claims.put("account", user_db.getAccount());
        // JWT 的 exp 和 Redis 登录态的 TTL 必须是同一个时长，否则会出现"token 没过期但 Redis 已经没了"
        Duration ttl = Duration.ofMinutes(expiration);
        String token = JwtUtil.generate_token(claims, ttl);
        // 将token存入redis（存的是 Duration，单位明确，别用 set(key, value, long) 那个秒为单位的重载）
        stringRedisTemplate.opsForValue().set(TOKEN_KEY + user_db.getAccount(), token, ttl);
        return Result.success(token);
    }

    /**
     * 注册
     * @param user
     * @return
     */
    @Override
    public Result sign(User user) {
        // 注册信息完整性校验
        if(user == null || StringUtils.isBlank(user.getPassword()) ||
                StringUtils.isBlank(user.getImageUrl()) || StringUtils.isBlank(user.getNickname()) ||
                StringUtils.isBlank(user.getPhone())){
            return Result.fail(400, "注册信息不完整，请重新填写");
        }
        // 校验手机号格式
        user.setPhone(user.getPhone().trim());
        if (!PHONE_PATTERN.matcher(user.getPhone()).matches()) {
            return Result.fail(400, "手机号格式不正确");
        }
        user.setPassword(user.getPassword().trim());
        if(user.getPassword().length() < 6 || user.getPassword().length() > 10){
            return Result.fail(400, "密码长度不在6~10这个范围");
        }
        // 必须先在 /user/register/check 验过验证码，否则可以绕过验证码直接注册任意手机号
        String verifiedKey = REGISTER_VERIFIED_KEY + user.getPhone();
        if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(verifiedKey))) {
            return Result.fail(400, "请先完成手机号验证码校验");
        }
        // 查询该手机号所对应用户是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone,user.getPhone());
        User user_db = userMapper.selectOne(wrapper);
        // 存在就返回fail
        if(user_db != null){
            return Result.fail(409, "该手机号已被注册");
        }
        // 不存在就生成唯一账号，加密密码
        String account = generate_global_account();
        user.setAccount(account);
        String encode_password = passwordEncoder.encode(user.getPassword());
        user.setPassword(encode_password);
        // 插入数据库 有全局异常，失败会被捕获
        userMapper.insert(user);
        // 注册成功后把"已验证"标记用掉，防止同一个校验结果被复用
        stringRedisTemplate.delete(verifiedKey);
        // 账号是注册成功后由服务端生成并返回给前端的唯一凭证，前端不需要也不能自己指定
        return Result.success(account);
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
        String key = "register:code:" + checkCode.getPhone();
        String failKey = "register:code:fail:" + checkCode.getPhone();
        String res = stringRedisTemplate.opsForValue().get(key);
        if (res == null) {
            throw new RedisDataException("验证码不存在或已过期");
        }
        if (!res.equals(checkCode.getCode())) {
            // 记录失败次数，达到上限就让验证码作废，必须重新获取
            Long fails = stringRedisTemplate.opsForValue().increment(failKey);
            if (fails != null && fails == 1L) {
                stringRedisTemplate.expire(failKey, Duration.ofMinutes(10));
            }
            if (fails != null && fails >= MAX_CODE_FAIL) {
                stringRedisTemplate.delete(key);
                stringRedisTemplate.delete(failKey);
                throw new RepeatCodeException("验证码错误次数过多，请重新获取");
            }
            return Result.fail(400, "验证码错误");
        }
        // 校验通过立刻消费掉，防止同一个验证码被重复使用
        stringRedisTemplate.delete(key);
        stringRedisTemplate.delete(failKey);
        // 打一个 5 分钟有效的"已验证"标记，注册接口凭它放行
        stringRedisTemplate.opsForValue()
                .set(REGISTER_VERIFIED_KEY + checkCode.getPhone(), "1", Duration.ofMinutes(5));
        return Result.success("验证码校验通过");
    }

    /**
     * 生成前端展示的验证码图片
     */
    @Override
    public void generate_code(String phone, HttpServletResponse response) throws IOException {
        // Hutool 的 LineCaptcha 写出的是 PNG 字节，这里必须写 image/png；
        // 同时禁止缓存，否则浏览器可能拿到上一张验证码
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(100, 40, 6, 3);
        // 将验证码存到redis
        String key = "register:code:" + phone;
        stringRedisTemplate.opsForValue().set(key, lineCaptcha.getCode(), Duration.ofMinutes(1));
        // 重新获取验证码时清空失败计数，避免用户被上次的错误次数连累
        stringRedisTemplate.delete("register:code:fail:" + phone);
        lineCaptcha.write(response.getOutputStream());
    }

    /**
     * 找回密码：生成验证码图片。
     * 逻辑和注册完全一致，只是 Redis key 换成 reset:code:{phone}
     */
    @Override
    public void generate_reset_code(String phone, HttpServletResponse response) throws IOException {
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(100, 40, 6, 3);
        stringRedisTemplate.opsForValue()
                .set(RESET_CODE_KEY + phone, lineCaptcha.getCode(), Duration.ofMinutes(1));
        stringRedisTemplate.delete(RESET_FAIL_KEY + phone);
        lineCaptcha.write(response.getOutputStream());
    }

    /**
     * 找回密码：校验验证码。
     * 与注册逻辑一致（错误次数上限 5 次、成功后消费验证码），
     * 区别是成功后额外打一个 reset:verified:{phone} 标记，供后续重置密码使用。
     */
    @Override
    public Result check_reset(CheckCode checkCode) {
        if (checkCode == null || StringUtils.isBlank(checkCode.getPhone()) || StringUtils.isBlank(checkCode.getCode())) {
            throw new CheckCodeDataException("找回密码时手机号发送验证码校验异常");
        }
        String key = RESET_CODE_KEY + checkCode.getPhone();
        String failKey = RESET_FAIL_KEY + checkCode.getPhone();
        String res = stringRedisTemplate.opsForValue().get(key);
        if (res == null) {
            throw new RedisDataException("验证码不存在或已过期");
        }
        if (!res.equals(checkCode.getCode())) {
            Long fails = stringRedisTemplate.opsForValue().increment(failKey);
            if (fails != null && fails == 1L) {
                stringRedisTemplate.expire(failKey, Duration.ofMinutes(10));
            }
            if (fails != null && fails >= MAX_CODE_FAIL) {
                stringRedisTemplate.delete(key);
                stringRedisTemplate.delete(failKey);
                throw new RepeatCodeException("验证码错误次数过多，请重新获取");
            }
            return Result.fail(400, "验证码错误");
        }
        // 通过即消费，并标记该手机号已校验，10 分钟内允许重置密码
        stringRedisTemplate.delete(key);
        stringRedisTemplate.delete(failKey);
        stringRedisTemplate.opsForValue()
                .set(RESET_VERIFIED_KEY + checkCode.getPhone(), "1", Duration.ofMinutes(10));
        return Result.success("验证码校验通过");
    }

    /**
     * 注册时上传头像并返回image_url
     * @param file
     * @return
     */
    @Override
    public Result upload(MultipartFile file) {
        return Result.success(ossUtil.upload_image(file));
    }

    /**
     * 忘记密码：重置密码
     * 前置条件：该手机号已通过 {@link #check_reset(CheckCode)} 校验（Redis 里有 reset:verified:{phone}）
     * @param user
     * @return
     */
    @Override
    public Result forget(User user) {
        if (user == null || StringUtils.isBlank(user.getPhone()) || StringUtils.isBlank(user.getPassword())) {
            return Result.fail(400, "手机号或新密码不能为空");
        }
        if (user.getPassword().length() < 6) {
            return Result.fail(400, "密码至少 6 位");
        }

        String verifiedKey = RESET_VERIFIED_KEY + user.getPhone();
        if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(verifiedKey))) {
            return Result.fail(400, "请先完成验证码校验");
        }

        User user_db = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, user.getPhone())
                .select(User::getId));
        if (user_db == null) {
            return Result.fail(404, "该手机号未注册");
        }

        int updated = userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getPhone, user.getPhone())
                .set(User::getPassword, passwordEncoder.encode(user.getPassword())));
        if (updated == 0) {
            throw new DBException("密码重置失败");
        }

        // 用完即焚：验证标记和残留的验证码/失败计数一起清掉
        stringRedisTemplate.delete(verifiedKey);
        stringRedisTemplate.delete(RESET_CODE_KEY + user.getPhone());
        stringRedisTemplate.delete(RESET_FAIL_KEY + user.getPhone());
        return Result.success("密码重置成功");
    }

    /**
     * 我的页面功能
     * @return
     */
    @Override
    public Result user_info() {
        // 获取登录信息
        LoginInfo loginInfo = UserContext.get();
        if(loginInfo == null){
            return Result.fail(401,"用户未登录，请先登录");
        }
        long id = Long.parseLong(loginInfo.getUserId());
        // 查询用户信息(头像、昵称)
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId,id).select(User::getImageUrl,User::getNickname);
        User user = userMapper.selectOne(wrapper);
        if(user == null){
            return Result.fail(401,"用户信息不存在");
        }
        // TODO查询关注数量、粉丝数量、动态数量

        // TODO封装信息
        return Result.success();
    }

    /**
     * 编辑个人信息前先查询个人信息
     */
    @Override
    public Result get_info() {
        // 获取登录信息
        LoginInfo loginInfo = UserContext.get();
        if(loginInfo == null){
            return Result.fail(401,"用户未登录，请先登录");
        }
        long id = Long.parseLong(loginInfo.getUserId());
        // 查询用户信息
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId,id).select(User::getAccount,User::getNickname,
                User::getAddress,User::getAge,User::getEmail,User::getGender,
                User::getPhone,User::getImageUrl);
        User user = userMapper.selectOne(wrapper);
        if(user == null){
            return Result.fail(401,"用户信息不存在");
        }
        return Result.success(user);
    }

    @Override
    public Result edit_info(User user) {
        // 获取登录信息
        LoginInfo loginInfo = UserContext.get();
        if(loginInfo == null){
            return Result.fail(401,"用户未登录，请先登录");
        }
        long id = Long.parseLong(loginInfo.getUserId());
        if(user == null){
            return Result.fail(401,"更新的个人信息未传递");
        }
        User update = new User();
        update.setId(id);
        update.setNickname(user.getNickname());
        update.setGender(user.getGender());
        update.setAge(user.getAge());
        update.setEmail(user.getEmail());
        update.setAddress(user.getAddress());
        update.setImageUrl(user.getImageUrl());
        // 更新用户信息
        int row = userMapper.updateById(update);
        if (row == 0) {
            throw new DBException("更新失败：记录不存在");
        }
        return Result.success("更新成功");
    }

    /**
     * 退出登录
     * @return
     */
    @Override
    public Result logout() {
        // 获取登录信息
        LoginInfo loginInfo = UserContext.get();
        if (loginInfo == null || StringUtils.isBlank(loginInfo.getAccount())) {
            return Result.fail(401, "用户未登录，请先登录");
        }
        String account = loginInfo.getAccount();
        // 删除redis中login:token
        stringRedisTemplate.delete("login:token:"+account);
        return Result.success("退出成功");
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
