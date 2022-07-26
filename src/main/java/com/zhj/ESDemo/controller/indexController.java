package com.zhj.ESDemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Topic
 * Description
 *
 * @author zhouh
 * @version 1.0
 * Create by 2022/7/24 21:12
 */
@Controller
public class indexController {
    @GetMapping({"/","/index"})
    public String index() {
        return "index";
    }
}
