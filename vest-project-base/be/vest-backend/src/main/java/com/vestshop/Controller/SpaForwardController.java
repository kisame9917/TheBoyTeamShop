package com.vestshop.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardController {

    // forward tất cả route client về index.html (trừ /api, /uploads, /images)
    @RequestMapping(value = {
            "/",
            "/shop",
            "/search",
            "/login",
            "/product/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}