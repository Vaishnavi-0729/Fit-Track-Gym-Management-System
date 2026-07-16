package com.example.javaproject.service;

import com.example.javaproject.model.AppUser;
import com.example.javaproject.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean registerUser(String username, String password) {

        if (appUserRepository.findByUsername(username).isPresent()) {
            return false;
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("STAFF");
        user.setEnabled(true);

        appUserRepository.save(user);

        return true;
    }
}