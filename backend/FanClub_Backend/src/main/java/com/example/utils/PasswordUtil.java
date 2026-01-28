package com.example.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {
    
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public static String encode(String password) {
        return passwordEncoder.encode(password);
    }
    
    public static boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}