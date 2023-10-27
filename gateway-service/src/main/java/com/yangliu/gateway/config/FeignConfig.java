package com.yangliu.gateway.config;

import feign.codec.Decoder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YL
 * @date 2023/10/27
 **/
@Configuration
public class FeignConfig {

    /**
     * 解决 HttpMessageConverters Bean未注册问题
     * @return
     */
    @Bean
    public Decoder feignDecoder(){
        //实现message的convert
        ObjectFactory<HttpMessageConverters> objectFactory = () -> {
            MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter =
                    new MappingJackson2HttpMessageConverter();
            List<MediaType> mediaTypeList = new ArrayList<>();
            mediaTypeList.add(MediaType.valueOf(MediaType.TEXT_HTML_VALUE + ";charset = UTF-8"));
            mappingJackson2HttpMessageConverter.setSupportedMediaTypes(mediaTypeList);
            final HttpMessageConverters httpMessageConverters =
                    new HttpMessageConverters(mappingJackson2HttpMessageConverter);
            return httpMessageConverters;
        };
        return new ResponseEntityDecoder(new SpringDecoder(objectFactory));
    }

}
