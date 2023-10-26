package com.yangliu.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * Ordered过滤器的顺序
 * @author YL
 * @date 2023/10/11
 **/
public class AuthFilter implements Ordered, GlobalFilter {

    //存放不需要进行token校验的路径（正则表达式）
    private Set<String> permitAll = new ConcurrentSkipListSet<>();
    //正则校验器
    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public AuthFilter(){
        permitAll.add("/**/oauth/**");
    }
    /**
     * chain过滤链条，过滤完后添加到chain中方便后续过滤器调用
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //根据路径进行路径校验是否放行
        String path = request.getURI().getPath();
        if (checkPermitAll(path)){
            return chain.filter(exchange);
        }

        return null;
    }

    /**
     * 校验路径是否可以放行
     * @param path
     * @return
     */
    private boolean checkPermitAll(String path) {
        return permitAll.stream().filter(p -> antPathMatcher.match(p,path))
                .findFirst().isPresent();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
