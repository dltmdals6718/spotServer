package com.example.spotserver.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CommentConditionRequest {

    @Max(value = 30, message = "max size : 30")
    private Integer size;

    @Min(value = 1, message = "페이지는 1이상여야야 합니다.")
    private Integer page;
    private String sort;

}
