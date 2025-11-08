package com.dss.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session) {
        // Nếu chưa đăng nhập, redirect về trang đăng nhập
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        // Nếu đã đăng nhập, redirect về dashboard
        return "redirect:/dashboard";
    }
}
