package com.example.javaproject.controller;

import com.example.javaproject.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class RegisterController {

    private final AppUserService appUserService;

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            return "register";
        }

        boolean registered = appUserService.registerUser(username, password);

        if (!registered) {
            model.addAttribute("error", "Username already exists!");
            return "register";
        }

        model.addAttribute("success", "Registration successful! Please login.");

        return "login";
    }
}