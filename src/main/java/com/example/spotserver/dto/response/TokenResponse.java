package com.example.spotserver.dto.response;

import lombok.Data;

@Data
public class TokenResponse {

    private String accessToken;
    private Long accessExpireIn;

    private String refreshToken;
    private Long refreshExpireIn;


}
