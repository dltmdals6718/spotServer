package com.example.spotserver.dto.response;

import com.example.spotserver.domain.Comment;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CommentResponse {

    private Long commentId;
    private Long writerId;
    private String writerName;
    private String memberImg;
    private String content;
    private LocalDateTime regDate;
    private Long likeCnt;

    @QueryProjection
    public CommentResponse(Long commentId, Long writerId, String writerName, String memberImg, String content, LocalDateTime regDate, Long likeCnt) {
        this.commentId = commentId;
        this.writerId = writerId;
        this.writerName = writerName;
        this.content = content;
        this.regDate = regDate;
        this.likeCnt = likeCnt;

        this.memberImg = memberImg;
        if (memberImg != null) {
            this.memberImg = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/members/" + writerId)
                    .path("/images/" + memberImg)
                    .toUriString();
        } else // 기본 이미지 경로
            this.memberImg = "";
    }
}
