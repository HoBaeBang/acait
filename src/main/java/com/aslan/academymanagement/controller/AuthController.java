package com.aslan.academymanagement.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/login-success")
    public String loginSuccess(@RequestParam String token) {
        return "<h1>Login Success!</h1><p>Your JWT Token:</p><textarea cols='100' rows='10'>" + token + "</textarea>";
    }
}
