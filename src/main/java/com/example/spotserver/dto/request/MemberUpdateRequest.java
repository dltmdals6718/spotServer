package com.example.spotserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberUpdateRequest {

    @NotBlank(message = "올바른 닉네임을 작성해주세요.")
    private String name;

    public void setName(String name) {
        this.name = name.trim();
    }
}
