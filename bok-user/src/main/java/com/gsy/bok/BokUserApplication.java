package com.gsy.bok;

import com.gsy.bokapi.clients.UserClient;
import com.gsy.bokapi.config.ApiConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(clients = {UserClient.class},defaultConfiguration = ApiConfiguration.class)
@SpringBootApplication
public class BokUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(BokUserApplication.class, args);
    }

}
