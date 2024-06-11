package com.example.spotserver.dto.request;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class LogoutRequest {
    private String refreshToken;
}
