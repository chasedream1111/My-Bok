package com.gsy.bok.interceptor;

import com.gsy.bok.domain.LoginInfo;
import com.gsy.bok.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class BokInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取请求头
        String userId = request.getHeader("userId");
        String account = request.getHeader("account");
        LoginInfo info = new LoginInfo();
        if(StringUtils.isNotBlank(userId)){
            info.setUserId(userId);
        }
        if(StringUtils.isNotBlank(account)){
            info.setAccount(account);
        }
        UserContext.set(info);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        UserContext.remove();
    }
}
