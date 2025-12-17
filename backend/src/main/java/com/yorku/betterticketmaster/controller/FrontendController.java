package com.yorku.betterticketmaster.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendController {

    @RequestMapping(value = {
        "/",
        "/login",
        "/search",
        "/event/**",
        "/checkout",
        "/organizer",
        "/admin"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
