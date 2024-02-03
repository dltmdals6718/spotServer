package com.example.spotserver.service;


import com.example.spotserver.domain.Comment;
import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.Poster;
import com.example.spotserver.dto.request.CommentRequest;
import com.example.spotserver.dto.response.CommentResponse;
import com.example.spotserver.dto.response.PageResponse;
import com.example.spotserver.exception.ErrorCode;
import com.example.spotserver.exception.PermissionException;
import com.example.spotserver.repository.CommentRepository;
import com.example.spotserver.repository.PosterRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class CommentService {
    private CommentRepository commentRepository;
    private PosterRepository posterRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, PosterRepository posterRepository) {
        this.commentRepository = commentRepository;
        this.posterRepository = posterRepository;
    }

    public CommentResponse addComment(Long posterId, CommentRequest commentRequest, Member member) {

        Poster poster = posterRepository.findById(posterId)
                .orElseThrow(() -> new NoSuchElementException());

        Comment comment = CommentRequest.toEntity(commentRequest);
        comment.setPoster(poster);
        comment.setWriter(member);

        Comment saveComment = commentRepository.save(comment);
        CommentResponse commentResponse = CommentResponse.toDto(saveComment);

        return commentResponse;
    }

    public PageResponse<List<CommentResponse>> getCommentsByPosterId(Long posterId, Pageable pageable) {
        Poster poster = posterRepository.findById(posterId)
                .orElseThrow(() -> new NoSuchElementException());
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

    public void deleteComment(Long commentId, Member member) throws PermissionException {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException());

        Member commentWriter = comment.getWriter();

        // writer.hashCode()랑 member.hashCode()가 일치함!
        if(commentWriter.getId() == member.getId()) {
            commentRepository.delete(comment);
        } else {
            throw new PermissionException(ErrorCode.FORBIDDEN_CLIENT);
        }
    }

    public CommentResponse updateComment(Long commentId, CommentRequest commentRequest, Member member) throws PermissionException {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException());

        Member commentWriter = comment.getWriter();

        if(commentWriter.getId().equals(member.getId())) {
            comment.setContent(commentRequest.getContent());
            return CommentResponse.toDto(comment);
        } else {
            throw new PermissionException(ErrorCode.FORBIDDEN_CLIENT);
        }

    }
}
