package com.example.spotserver.dto.response;

import com.example.spotserver.domain.Comment;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponse {

    private Long commentId;
    private Long writerId;
    private String writerName;
    private String content;
    private LocalDateTime regDate;


    public static CommentResponse toDto(Comment comment) {
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setCommentId(comment.getId());
        commentResponse.setWriterId(comment.getWriter().getId());
        commentResponse.setWriterName(comment.getWriter().getName());
        commentResponse.setContent(comment.getContent());
        commentResponse.setRegDate(comment.getRegDate());
        return commentResponse;
    }

}
