package com.example.spotserver.domain;

import lombok.Data;

@Data
public class PageInfo {

    private int page;
    private int size;
    private Long numberOfElements;
    private Long totalElements;
    private int totalPage;

}
