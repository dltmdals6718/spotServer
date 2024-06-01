package com.example.spotserver.config.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
public class JwtProperties {

    public static String SECRET_KEY;
    public static Long ACCESS_TOKEN_EXPIRE_TIME = 60L;
    public static Long REFRESH_TOKEN_EXPIRE_TIME = 60L * 3;
    public static String TOKEN_PREFIX = "Bearer ";

    @Value("${jwt.secrectKey}")
    public void setSecretKey(String secretKey) {
        SECRET_KEY = secretKey;
    }
}
