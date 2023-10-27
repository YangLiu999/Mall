package com.yangliu.gateway.filter;

import com.yangliu.gateway.feignClient.Oauth2ServiceClient;
import lombok.SneakyThrows;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * Ordered过滤器的顺序
 * @author YL
 * @date 2023/10/11
 **/
@Component
public class AuthFilter implements Ordered, GlobalFilter {
    /**
     * 通过Autowired我们引入feignclient会发生死锁，springcloud gateway基于netty的，是webFlux的响应式的编程。
     * 引入feignclient，在代码设计层面Loaded RoutePredicateFactory会造成死锁，导致无法启动
     * 需要添加@Lazy礼让我们的springcloud gateway的webflux相关的加载
     */
    @Autowired
    @Lazy
    private Oauth2ServiceClient oauth2ServiceClient;

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
    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //根据路径进行路径校验是否放行
        String path = request.getURI().getPath();
        if (checkPermitAll(path)){
            return chain.filter(exchange);
        }
        String token = request.getHeaders().getFirst("Authorization");
        /**
         * 报错在block()/blockFirst()/blockLast() are blocking, which is not supported in thread reactor-http-nio-3
         * reactor 响应式编程就是基于 reactor的；
         * Map<String, Object> result = oauth2ServiceClient.checkToken(token)是命令式编程，会发生冲突
         */
        //Map<String, Object> result = oauth2ServiceClient.checkToken(token);//同步rest请求
        //改为异步
        /**
         * 报错 Feign.codec.DecodeException: No qualifying bean of type
         * 'org.springframework.boot.autoconfigure.http.HttpMessageConverters' available:
         * expected at least 1 bean which qualifies as autowire candidate.
         * HttpMessageConverters 这个是我们http请求的msg转化器，openfeign和gateway的调用机制有冲突
         */
        CompletableFuture<Map> future = CompletableFuture.supplyAsync(() -> oauth2ServiceClient.checkToken(token));
        Map<String,Object> result = future.get();

        boolean active = (boolean) result.get("Active");
        if (!active){
            //验证未通过
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //通过后可以给微服务发请求时带上一些Header
        ServerHttpRequest httpRequest = request.mutate().headers(httpHeaders -> {
            httpHeaders.set("personId","");
            httpHeaders.set("traceId","");
        }).build();
        exchange.mutate().request(httpRequest);
        return response.setComplete();
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
