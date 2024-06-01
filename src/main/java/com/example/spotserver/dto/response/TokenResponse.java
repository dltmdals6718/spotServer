package com.example.spotserver.dto.response;

import lombok.Data;

@Data
public class TokenResponse {

//    private String accessToken;
//    private String accessExpireIn;
    private String token;
    private Long expire_in;

    private String refreshToken;
    private Long refreshExpireIn;


}
