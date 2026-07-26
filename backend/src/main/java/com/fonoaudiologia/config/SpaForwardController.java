package com.fonoaudiologia.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class SpaForwardController {

    @GetMapping(value = {"/{path:^(?!api|login|logout|assets|favicon).*}/**"})
    public void forward(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Resource resource = new ClassPathResource("static/index.html");
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        StreamUtils.copy(resource.getInputStream(), response.getOutputStream());
    }
}
