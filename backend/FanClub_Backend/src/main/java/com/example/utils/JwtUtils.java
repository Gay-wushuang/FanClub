package com.example.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    private final SecretKey secretKey;
    private final long expirationTime;

    public JwtUtils(
            @Value("${jwt.secret}") String secretString,
            @Value("${jwt.expiration:86400000}") long expirationTime
    ) {
        // 验证并创建密钥
        if (secretString.length() < 32) {
            throw new IllegalArgumentException("JWT密钥必须至少32字符");
        }
        this.secretKey = Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(secretString)
        );
        this.expirationTime = expirationTime;
    }

    public String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public static Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("令牌已过期", e);
        } catch (SecurityException | MalformedJwtException e) {
            throw new RuntimeException("令牌无效", e);
        } catch (Exception e) {
            throw new RuntimeException("令牌解析失败", e);
        }
    }
}