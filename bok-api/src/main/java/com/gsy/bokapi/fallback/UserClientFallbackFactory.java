package com.gsy.bokapi.fallback;

import com.gsy.bokapi.clients.UserClient;
import org.springframework.cloud.openfeign.FallbackFactory;

public class UserClientFallbackFactory implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        return null;
    }
}
