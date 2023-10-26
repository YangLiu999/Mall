package com.yangliu.gateway.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author YL
 * @date 2023/10/11
 **/
@FeignClient("oauth2-service")
public interface Oauth2ServiceClient {
    /**
     * token check
     * @param token
     * @return
     */
    @RequestMapping("/oauth/check_token")
    Map<String,Object> checkToken(@RequestParam("token") String token);

}
