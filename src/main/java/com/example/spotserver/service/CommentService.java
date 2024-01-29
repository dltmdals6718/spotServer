package com.example.spotserver.service;


import com.example.spotserver.domain.Comment;
import com.example.spotserver.domain.Poster;
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

    public void addComment(Comment comment) {
        commentRepository.save(comment);
    }

    public PageResponse<List<CommentResponse>> getCommentsByPosterId(Long posterId, Pageable pageable) {
        Poster poster = posterService.getPoster(posterId);
        Page<Comment> page = commentRepository.findCommentsByPoster(poster, pageable);
        Page<CommentResponse> dtoPage = page.map((comment) -> CommentResponse.toDto(comment));
        PageResponse<List<CommentResponse>> pageResponse = new PageResponse<>(dtoPage);
        return pageResponse;
    }

    public Comment getComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException());
        return comment;
    }
}
