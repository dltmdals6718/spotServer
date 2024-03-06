package com.example.spotserver.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
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

    @QueryProjection
    public PosterResponse(Long posterId, Long writerId, String writerName, String title, String content, LocalDateTime regDate, Long likeCnt, Long commentCnt) {
        this.posterId = posterId;
        this.writerId = writerId;
        this.writerName = writerName;
        this.title = title;
        this.content = content;
        this.regDate = regDate;
        this.likeCnt = likeCnt;
        this.commentCnt = commentCnt;
    }
}
