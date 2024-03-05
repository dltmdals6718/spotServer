package com.example.spotserver.config.kakao;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KakaoConfig {
    public static String CLIENT_ID;
    public static String REDIRECT_URI;


    @Value("${kakao.clientId}")
    public void setClientId(String clientId) {
        CLIENT_ID = clientId;
    }

    @Value("${kakao.redirectUri}")
    public void setRedirectUri(String redirectUri) {
        REDIRECT_URI = redirectUri;
    }
}
