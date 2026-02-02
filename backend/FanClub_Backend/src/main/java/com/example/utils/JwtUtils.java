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

public class JwtUtils {

    private static SecretKey secretKey;
    private static long expirationTime;

    public static void init(String secretString, long expiration) {
        // 验证并创建密钥
        if (secretString.length() < 16) {
            throw new IllegalArgumentException("JWT密钥必须至少16字符");
        }
        // 确保密钥长度足够
        while (secretString.length() < 32) {
            secretString += secretString;
        }
        secretKey = Keys.hmacShaKeyFor(
                secretString.getBytes()
        );
        expirationTime = expiration;
    }

    public static String generateToken(Map<String, Object> claims) {
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