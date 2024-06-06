package com.example.spotserver.dto.response;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class AccessTokenResponse {

    private String accessToken;
    private Long accessExpireIn;

}
