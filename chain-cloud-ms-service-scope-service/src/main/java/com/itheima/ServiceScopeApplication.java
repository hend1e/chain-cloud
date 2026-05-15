package com.itheima;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServiceScopeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceScopeApplication.class, args);
    }

}
