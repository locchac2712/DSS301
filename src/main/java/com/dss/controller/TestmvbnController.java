package com.dss.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestmvbnController {

    @GetMapping("/home")
    public String home() {
        return "Hello from TestController";
    }
}


