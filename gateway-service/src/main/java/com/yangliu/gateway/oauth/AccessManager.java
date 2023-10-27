package com.yangliu.gateway.oauth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author YL
 * @date 2023/08/22
 **/
@Component
public class AccessManager implements ReactiveAuthorizationManager<AuthorizationContext> {//响应式

    //存放不需要进行token校验的路径（正则表达式）
    private  Set<String> permitAll = new ConcurrentSkipListSet<>();
    //正则校验器
    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public AccessManager(){
        //可以放行的路径
        permitAll.add("/**/oauth/**");
    }

    //webFlux Mono Flux
    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication,

                                             AuthorizationContext authorizationContext) {
        //校验核心代码
        ServerWebExchange exchange = authorizationContext.getExchange();
        return authentication.map(auth -> {
           String requestPath = exchange.getRequest().getURI().getPath();
           //放行URL校验
           if (checkPermitAll(requestPath)){
                return new AuthorizationDecision(true);
           }
           //判断是否是OAuth2Authentication类型
           if (auth instanceof OAuth2Authentication){
               OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) auth;
               String clientId = oAuth2Authentication.getOAuth2Request().getClientId();
               if (StringUtils.isNotEmpty(clientId)){
                   return new AuthorizationDecision(true);
               }
           }
           return new AuthorizationDecision(false);
        });
    }

    /**
     * 校验请求路径是否放行
     * @param requestPath
     * @return
     */
    private boolean checkPermitAll(String requestPath) {
        return permitAll.stream().filter(p -> antPathMatcher.match(p,requestPath))
                .findFirst().isPresent();
    }
}
