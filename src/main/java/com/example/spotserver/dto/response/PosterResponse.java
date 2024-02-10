package com.example.spotserver.dto.response;

import com.example.spotserver.domain.Poster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PosterResponse {

    private Long posterId;
    private Long writerId;
    private String title;
    private String content;
    private LocalDateTime regDate;
    private Long likeCnt;
    private Long commentCnt;


    public static PosterResponse toDto(Poster poster) {
        PosterResponse posterResponse = new PosterResponse();
        posterResponse.setPosterId(poster.getId());
        posterResponse.setWriterId(poster.getWriter().getId());
        posterResponse.setTitle(poster.getTitle());
        posterResponse.setContent(poster.getContent());
        posterResponse.setRegDate(poster.getRegDate());
        return posterResponse;
    }
}
