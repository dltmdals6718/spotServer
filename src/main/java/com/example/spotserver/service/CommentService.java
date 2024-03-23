package com.example.spotserver.service;


import com.example.spotserver.domain.Comment;
import com.example.spotserver.domain.CommentLike;
import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.Poster;
import com.example.spotserver.dto.request.CommentConditionRequest;
import com.example.spotserver.dto.request.CommentRequest;
import com.example.spotserver.dto.response.CommentResponse;
import com.example.spotserver.dto.response.PageResponse;
import com.example.spotserver.exception.DuplicateException;
import com.example.spotserver.exception.ErrorCode;
import com.example.spotserver.exception.PermissionException;
import com.example.spotserver.repository.CommentLikeRepository;
import com.example.spotserver.repository.CommentRepository;
import com.example.spotserver.repository.MemberRepository;
import com.example.spotserver.repository.PosterRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class CommentService {
    private CommentRepository commentRepository;
    private PosterRepository posterRepository;
    private CommentLikeRepository commentLikeRepository;
    private MemberRepository memberRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, PosterRepository posterRepository, CommentLikeRepository commentLikeRepository, MemberRepository memberRepository) {
        this.commentRepository = commentRepository;
        this.posterRepository = posterRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.memberRepository = memberRepository;
    }

    public Long addComment(Long posterId, Comment comment, Long memberId) {

        Poster poster = posterRepository.findById(posterId)
                .orElseThrow(() -> new NoSuchElementException());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException());


        comment.setPoster(poster);
        comment.setWriter(member);

        commentRepository.save(comment);
        return comment.getId();
    }

    public PageResponse<CommentResponse> getComments(Long posterId, CommentConditionRequest commentConditionRequest) {
        Page<CommentResponse> comments = commentRepository.getComments(posterId, commentConditionRequest);
        PageResponse<CommentResponse> pageResponse = new PageResponse<>(comments);
        return pageResponse;
    }

    public CommentResponse getComment(Long commentId) {
        CommentResponse commentResponse = commentRepository.getCommentById(commentId);
        if(commentResponse==null)
            throw new NoSuchElementException();
        return commentResponse;
    }

    public void deleteComment(Long commentId, Long memberId) throws PermissionException {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException());

        Member commentWriter = comment.getWriter();

        // writer.hashCode()랑 member.hashCode()가 일치함!
        if (commentWriter.getId().equals(member.getId())) {
            commentRepository.delete(comment);
        } else {
            throw new PermissionException(ErrorCode.FORBIDDEN_CLIENT);
        }
    }

    public void updateComment(Long commentId, CommentRequest commentRequest, Long memberId) throws PermissionException {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException());

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException());

        Member commentWriter = comment.getWriter();

        if (commentWriter.getId().equals(member.getId())) {
            comment.setContent(commentRequest.getContent());
        } else {
            throw new PermissionException(ErrorCode.FORBIDDEN_CLIENT);
        }

    }

    public void addLike(Long commentId, Long memberId) throws DuplicateException {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException());
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException());

        if (commentLikeRepository.existsByCommentAndMember(comment, member))
            throw new DuplicateException(ErrorCode.DUPLICATE_LIKE);

        CommentLike commentLike = new CommentLike();
        commentLike.setMember(member);
        commentLike.setComment(comment);
        commentLikeRepository.save(commentLike);
    }

    public void deleteLike(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException());

        CommentLike commentLike = commentLikeRepository.findByCommentAndMember(comment, member)
                .orElseThrow(() -> new NoSuchElementException());

        commentLikeRepository.delete(commentLike);
    }

}
