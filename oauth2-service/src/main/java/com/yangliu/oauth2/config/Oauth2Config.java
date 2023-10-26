package com.yangliu.oauth2.config;

import com.yangliu.oauth2.service.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.annotation.Resource;
import javax.sql.DataSource;


/**
 * @author YL
 * @date 2023/07/25
 **/
@Configuration
@EnableAuthorizationServer
public class Oauth2Config extends AuthorizationServerConfigurerAdapter {

    /**
     * @Autowired是按类型查找Bean，@Resource是按名查找
     */
    @Resource
    private DataSource dataSource;
    @Autowired
    private UserDetailServiceImpl userDetailService;
    @Resource
    private AuthenticationManager manager;

    /**
     * oauth2为了生成token令牌，首先解决token令牌存储位置
     */
    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    /**
     *oauth2默认的token过期时间是12小时，自定义过期时间需要使用defaultTokenService并进行set
     */
    @Bean
    @Primary
    public DefaultTokenServices defaultTokenServices(){
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        //30天过期
        defaultTokenServices.setAccessTokenValiditySeconds(30 * 24 * 3600);
        //设置存储位置
        defaultTokenServices.setTokenStore(tokenStore());
        return defaultTokenServices;
    }



    /**
     * client details表中的client_id和client_secret
     */
    @Bean
    public ClientDetailsService clientDetails(){
        return new JdbcClientDetailsService(dataSource);
    }

    /**
     * 通过ClientDetailsServiceConfig将ClientDetailsService设置到oauth中
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetails());
    }

    /**
     * 存储用户密码和client_secret需要加密算法
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        //需要自己实现加密算法，可以通过这种方式encode和校验
        /*return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return null;
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return false;
            }
        }*/
        return new BCryptPasswordEncoder();
    }

    /**
     *添加自定义的安全配置，可以不添加
     * 场景：用于放开一些接口的查询权限，比如说checkToken接口
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
            //可以进行表单验证
        security.allowFormAuthenticationForClients()
                //checkToken
                .checkTokenAccess("permitAll");
    }

    /**
     * 处理UserDetailServiceImpl接口
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .userDetailsService(userDetailService)
                .tokenServices(defaultTokenServices())
                .authenticationManager(manager)
                .tokenStore(tokenStore());
    }
}
