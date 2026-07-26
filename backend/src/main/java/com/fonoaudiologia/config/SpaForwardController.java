package com.fonoaudiologia.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping(value = {"/{path:^(?!api|login|logout).*}/**"})
    public String forward() {
        return "forward:/index.html";
    }
}
