package com.yangliu.gateway.oauth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * @author YL
 * @date 2023/09/13
 **/
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Resource
    private DataSource dataSource;
    @Resource
    private AccessManager accessManager;

    @Bean
    SecurityWebFilterChain webFluxSecurityWebFilterChain(ServerHttpSecurity serverHttpSecurity){

        //reactiveAuthenticationManager权限管理器需要底层DB资源管理类dataSource
        ReactiveAuthenticationManager reactiveAuthenticationManager =
                new ReactiveJdbcAuthenticationManager(dataSource);

        //authenticationWebFilter需要必要组件reactiveAuthenticationManager响应式权限管理器
        AuthenticationWebFilter authenticationWebFilter =
                new AuthenticationWebFilter(reactiveAuthenticationManager);
        authenticationWebFilter.
                setServerAuthenticationConverter(new ServerBearerTokenAuthenticationConverter());

        serverHttpSecurity.httpBasic().disable()
                .csrf().disable()
                .authorizeExchange()
                //每次前端使用ajax调用后台接口回先发送HttpMethod.OPTIONS，所以需要放行
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .anyExchange().access(accessManager)
                .and().addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        return serverHttpSecurity.build();
    }
}
