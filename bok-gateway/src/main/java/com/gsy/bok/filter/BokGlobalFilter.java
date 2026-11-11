package com.gsy.bok.filter;

import com.gsy.bok.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class BokGlobalFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取request
        ServerHttpRequest request = exchange.getRequest();
        RequestPath path = request.getPath();
        // 登录、注册（含验证码、上传头像）、健康检查不需要 token
        if (isWhitelisted(path.toString())) {
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isBlank(token)) {
            return unauthorized(exchange, "缺少 token");
        }
        // 兼容前端标准的 "Bearer xxx" 写法
        if (token.startsWith(BEARER_PREFIX)) {
            token = token.substring(BEARER_PREFIX.length()).trim();
        }

        Claims claims;
        try {
            claims = JwtUtil.parse_token(token);
        } catch (ExpiredJwtException e) {
            return unauthorized(exchange, "token 已过期，请重新登录");
        } catch (JwtException | IllegalArgumentException e) {
            return unauthorized(exchange, "token 无效");
        }

        Object userId = claims.get("userId");
        Object account = claims.get("account");

        if (userId == null || account == null) {
            return unauthorized(exchange, "token 中缺少用户信息");
        }

        // 先构造带上用户信息的请求，再转发给下游（否则 bok-user 侧拿不到 userId）
        ServerWebExchange webExchange = exchange.mutate()
                .request(builder -> builder
                        .header("userId", String.valueOf(userId))
                        .header("account", String.valueOf(account)))
                .build();

        // 再校验 Redis 白名单：登出 / 被顶号 / 改密后会没有这个 key
        return redisTemplate.hasKey("login:token:" + account)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? chain.filter(webExchange)                        // 校验通过，放行
                        : unauthorized(exchange, "登录已过期，请重新登录"))  // 已过期/已登出
                .onErrorResume(e -> unauthorized(exchange, "登录状态校验失败"));
    }

    private boolean isWhitelisted(String path) {
        // 注意：AntPathMatcher 里 "/user/register/**" 匹配不到 "/user/register" 本身，
        // 注册提交、头像上传都是无斜杠路径，必须单独列一条
        return antPathMatcher.match("/user/login", path)
                || antPathMatcher.match("/user/login/**", path)
                || antPathMatcher.match("/user/register", path)
                || antPathMatcher.match("/user/register/**", path)
                || antPathMatcher.match("/user/upload", path)
                || antPathMatcher.match("/user/upload/**", path)
                || antPathMatcher.match("/actuator/**", path);
    }

    /**
     * 直接在过滤器里写 401 响应。不要 throw 自定义异常：
     * Gateway 过滤器抛出的异常走 WebFlux 的 WebExceptionHandler，
     * @RestControllerAdvice 那套接不住，最后会变成默认的 500。
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        String body = "{\"code\":401,\"msg\":\"" + message + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
