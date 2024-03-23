package com.example.spotserver.repository;

import com.example.spotserver.dto.request.CommentConditionRequest;
import com.example.spotserver.dto.response.CommentResponse;
import org.springframework.data.domain.Page;

public interface CommentRepositoryCustom {

    Page<CommentResponse> getComments(Long posterId, CommentConditionRequest commentConditionRequest);
    CommentResponse getCommentById(Long commentId);

}
