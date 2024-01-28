package com.example.spotserver.dto.response;


import com.example.spotserver.domain.PageInfo;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageResponse<T> {

    private List<T> results;
    private PageInfo pageInfo;


    public PageResponse(Page page) {
        results = page.getContent();
        pageInfo = new PageInfo();
        pageInfo.setPage(page.getNumber()+1); // Page의 페이지는 0부터 시작이라 사용자를 위해 +1해서 1부터 시작하게함
        pageInfo.setSize(page.getSize());
        pageInfo.setTotalPage(page.getTotalPages()); // 총 페이지 개수
        pageInfo.setTotalElements(page.getTotalElements()); // 총 원소 개수
        pageInfo.setNumberOfElements(page.getNumberOfElements()); // 현재 페이지에서 원소 개수
    }
}
