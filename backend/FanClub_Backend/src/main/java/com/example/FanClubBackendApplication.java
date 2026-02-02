package com.example;

import com.example.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FanClubBackendApplication implements CommandLineRunner {

    @Value("${jwt.secret:default-secret-key-for-development-only-which-is-at-least-32-characters-long}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

	public static void main(String[] args) {
		SpringApplication.run(FanClubBackendApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
        // 初始化 JwtUtils
        JwtUtils.init(jwtSecret, jwtExpiration);
    }

}
