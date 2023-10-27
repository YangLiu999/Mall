package com.yangliu.gateway.oauth;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;

/**
 * ReactiveAuthenticationManager响应式权限管理器
 * @author YL
 * @date 2023/09/14
 **/
public class ReactiveJdbcAuthenticationManager implements ReactiveAuthenticationManager {
    /**
     * 这个tokenstore在oauth2的service中，通过datasource创建
     * datasource在securityConfig里边
     */
    private TokenStore tokenStore;

    public ReactiveJdbcAuthenticationManager(DataSource dataSource){
        this.tokenStore = new JdbcTokenStore(dataSource);
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                //后续会通过将token放到http header Authorization 中进行访问,并且会协带bearer前缀
                .filter(auth -> auth instanceof BearerTokenAuthenticationToken)
                .cast(BearerTokenAuthenticationToken.class)
                //获取token进行操作
                .map(BearerTokenAuthenticationToken :: getToken)
                .flatMap(accessToken -> {
                    OAuth2AccessToken oAuth2AccessToken = this.tokenStore.readAccessToken(accessToken);
                    if (oAuth2AccessToken == null){//db中不存在
                        return Mono.error(new InvalidTokenException("InvalidTokenException"));
                    }else if (oAuth2AccessToken.isExpired()){//token 过期
                        return Mono.error(new InvalidTokenException("isExpired"));
                    }
                    //判断是否是oauth2的token，防止非法token插入db设置了永久存在，通过gateway来访问
                    OAuth2Authentication oAuth2Authentication = this.tokenStore.readAuthentication(accessToken);
                    if (oAuth2Authentication == null){
                        return Mono.error(new InvalidTokenException("fake token"));
                    }
                    return Mono.justOrEmpty(oAuth2Authentication);
                })
                .cast(Authentication.class);
    }


}
