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

        /*
         todo: 기본 PosterRepository find 사용하여 toDto 사용시 likeCnt, commentCnt가 null로 나옴 -> 단건도 댓글, 좋아요 개수 포함, 조회결과 없는거 예외처리 필요
         CustomRepository에 단건 조회도 추가하자.
         */

        return posterResponse;
    }
}
