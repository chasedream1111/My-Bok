package com.gsy.bokapi.clients;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value="bok-user")
public interface UserClient {


}
