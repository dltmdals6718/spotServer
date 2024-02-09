package com.example.spotserver.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


@Data
public class PosterPageRequest {


    @Min(value = 1, message = "페이지는 1이상여야야 합니다.")
    private int page = 1;

    @Max(value = 30, message = "페이지 크기는 30을 넘을 수 없습니다.")
    @Min(value = 1, message = "페이지 크기는 1이상이여야 합니다.")
    private int size = 10;

    private String sort = "recent";

    public org.springframework.data.domain.PageRequest makePageRequest() {
        return org.springframework.data.domain.PageRequest.of(page-1,size);
    }
}
