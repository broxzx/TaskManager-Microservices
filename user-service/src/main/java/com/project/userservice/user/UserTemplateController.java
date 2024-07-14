package com.project.userservice.user;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/users")
@Slf4j
public class UserTemplateController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/changePassword")
    public String changePassword(@RequestParam(value = "token", required = false) String token, Model model) {
        model.addAttribute("token", token);
        return "changePassword";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request) {

        log.info("{}", request.getHeader(HttpHeaders.AUTHORIZATION));
        return "dashboard";
    }

}
