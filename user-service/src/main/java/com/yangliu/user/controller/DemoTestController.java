package com.yangliu.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author YL
 * @date 2023/07/11
 **/
@RestController
public class DemoTestController {

    @GetMapping
    public String test(){
        return "hello";
    }
}
