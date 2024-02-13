package com.example.spotserver.dto.request;

import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class LocationConditionRequest {

    @Max(value = 30, message = "max size : 30")
    private Integer size;
    private Integer page;
    private String sort;
    private String search;

}
