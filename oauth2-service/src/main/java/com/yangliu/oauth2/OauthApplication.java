package com.yangliu.oauth2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

/**
 * @author YL
 * @date 2023/07/11
 * EnableResourceServer：权限校验资源
 **/
@EnableDiscoveryClient
@SpringBootApplication
@EnableResourceServer
public class OauthApplication {
    public static void main(String[] args) {
        SpringApplication.run(OauthApplication.class, args);
    }
}
