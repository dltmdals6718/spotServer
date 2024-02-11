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
    private String writerName;
    private String title;
    private String content;
    private LocalDateTime regDate;
    private Long likeCnt;
    private Long commentCnt;



    // todo: toDto를 쓰는 게시글 등록, 수정은 likeCnt, commentCnt가 null로 나옴. 0으로 초기화할까 아님 조회 쿼리를? 아님 다른방법?
    public static PosterResponse toDto(Poster poster) {
        PosterResponse posterResponse = new PosterResponse();
        posterResponse.setPosterId(poster.getId());
        posterResponse.setWriterId(poster.getWriter().getId());
        posterResponse.setWriterName(poster.getWriter().getName());
        posterResponse.setTitle(poster.getTitle());
        posterResponse.setContent(poster.getContent());
        posterResponse.setRegDate(poster.getRegDate());


        return posterResponse;
    }
}
