package com.gsy.bok.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class BokGlobalFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取request
        ServerHttpRequest request = exchange.getRequest();
        //判断是否是登录注册
        RequestPath path = request.getPath();
        if(antPathMatcher.match("/user/login/**",path.toString()) || antPathMatcher.match("/user/sign/**",path.toString())){
            return chain.filter(exchange);
        }
        //获取token
        HttpHeaders headers = request.getHeaders();
        String token = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if(token == null){
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.NON_AUTHORITATIVE_INFORMATION);
            return response.setComplete();
        }
        //解析token TODO利用JWT解析
        Long userId = 1L;
        //将解析后的信息放入请求头
        ServerWebExchange webExchange = exchange.mutate()
                .request(builder -> builder.header("user-info", userId.toString()))
                .build();
        //放行
        return chain.filter(webExchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
