package com.gsy.bok.controller;

import com.gsy.bok.domain.CheckCode;
import com.gsy.bok.domain.User;
import com.gsy.bok.service.UserService;
import com.gsy.bok.utils.Result;
import com.gsy.bok.utils.Results;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
    *  登录
    *  可以使用账号+密码
    * */
    @PostMapping("/login")
    public ResponseEntity<Result<?>> login(@RequestBody User user){
        return Results.toResponse(userService.login(user));
    }

    /**
     * 注册
     * 注册时使用手机号+验证码，设置密码，填写基本信息
     * 注意：不需要传 account，账号由服务端生成，注册成功后返回
     **/
    @PostMapping("/register")
    public ResponseEntity<Result<?>> register(@RequestBody User user){
        return Results.toResponse(userService.sign(user));
    }

    /**
     * 注册时发送的验证码，验证手机号是否正确，用户填写后点击发送右边的校验触发，校验通过下面信息才能填
     */
    @PostMapping("/register/check")
    public ResponseEntity<Result<?>> check_code(@RequestBody CheckCode checkCode){
        return Results.toResponse(userService.check(checkCode));
    }

    /**
     * 注册时上传头像到OSS并返回image_url给前端
     */

    /**
     * 注册时前端展示的验证码
     */
    @GetMapping("/register/generate_code/{phone}")
    public void generate_code(@PathVariable("phone") String phone, HttpServletResponse response) throws IOException {
        userService.generate_code(phone,response);
    }
}
