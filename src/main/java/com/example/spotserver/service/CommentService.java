package com.example.spotserver.service;


import com.example.spotserver.domain.Comment;
import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.Poster;
import com.example.spotserver.dto.request.CommentRequest;
import com.example.spotserver.dto.response.CommentResponse;
import com.example.spotserver.dto.response.PageResponse;
import com.example.spotserver.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CommentService {
    private CommentRepository commentRepository;
    private PosterService posterService;

    @Autowired
    public CommentService(CommentRepository commentRepository, PosterService posterService) {
        this.commentRepository = commentRepository;
        this.posterService = posterService;
    }

    public CommentResponse addComment(Long posterId, CommentRequest commentRequest, Member member) {

        Poster poster = posterService.getPoster(posterId);

        Comment comment = CommentRequest.toEntity(commentRequest);
        comment.setPoster(poster);
        comment.setWriter(member);

        Comment saveComment = commentRepository.save(comment);
        CommentResponse commentResponse = CommentResponse.toDto(saveComment);

        return commentResponse;
    }

    public PageResponse<List<CommentResponse>> getCommentsByPosterId(Long posterId, Pageable pageable) {
        Poster poster = posterService.getPoster(posterId);
        Page<Comment> page = commentRepository.findCommentsByPoster(poster, pageable);
        Page<CommentResponse> dtoPage = page.map((comment) -> CommentResponse.toDto(comment));
        PageResponse<List<CommentResponse>> pageResponse = new PageResponse<>(dtoPage);
        return pageResponse;
    }

    public CommentResponse getComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException());

        CommentResponse commentResponse = CommentResponse.toDto(comment);

        return commentResponse;
    }
}
