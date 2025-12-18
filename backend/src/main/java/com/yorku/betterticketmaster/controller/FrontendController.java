package com.yorku.betterticketmaster.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Forwards SPA routes to the frontend index.html.
 */
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
    /**
     * Forward known frontend routes to index.html.
     * @return forward path
     */
    public String forward() {
        return "forward:/index.html";
    }
}
