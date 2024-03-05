package com.example.spotserver.config.kakao;

import lombok.Data;

@Data
public class KakaoToken {

    String access_token;
    Integer expires_in;

    String refresh_token;
    Integer refresh_token_expires_in;
}
