package com.example.spotserver.controller;

import com.example.spotserver.domain.Comment;
import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.Poster;
import com.example.spotserver.dto.request.CommentConditionRequest;
import com.example.spotserver.dto.request.CommentPageRequest;
import com.example.spotserver.dto.request.CommentRequest;
import com.example.spotserver.dto.response.CommentResponse;
import com.example.spotserver.dto.response.PageResponse;
import com.example.spotserver.exception.DuplicateException;
import com.example.spotserver.exception.PermissionException;
import com.example.spotserver.repository.PosterRepository;
import com.example.spotserver.service.CommentService;
import com.example.spotserver.service.PosterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class CommentController {

    private CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/comments/{posterId}")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long posterId,
                                                      @Valid @RequestBody CommentRequest commentRequest,
                                                      @AuthenticationPrincipal(expression = "member") Member member) {

        CommentResponse commentResponse = commentService.addComment(posterId, commentRequest, member);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commentResponse);
    }

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> getComment(@PathVariable Long commentId) {

        CommentResponse commentResponse = commentService.getComment(commentId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentResponse);
    }

    @GetMapping("/posters/{posterId}/comments")
    public ResponseEntity<PageResponse<List<CommentResponse>>> getComments(@PathVariable Long posterId,
                                                                           @Valid CommentConditionRequest commentConditionRequest) {

        PageResponse<List<CommentResponse>> comments = commentService.getComments(posterId, commentConditionRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(comments);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity deleteComment(@PathVariable Long commentId,
                                        @AuthenticationPrincipal(expression = "member") Member member) throws PermissionException {

        commentService.deleteComment(commentId, member);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long commentId,
                                                         @Valid @RequestBody CommentRequest commentRequest,
                                                         @AuthenticationPrincipal(expression = "member") Member member) throws PermissionException {

        CommentResponse commentResponse = commentService.updateComment(commentId, commentRequest, member);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentResponse);
    }

    @PostMapping(value = "/comments/{commentId}/likes")
    public ResponseEntity addLike(@PathVariable Long commentId,
                                  @AuthenticationPrincipal(expression = "member") Member member) throws DuplicateException {

        commentService.addLike(commentId, member);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @DeleteMapping(value = "/comments/{commentId}/likes")
    public ResponseEntity deleteLike(@PathVariable Long commentId,
                                     @AuthenticationPrincipal(expression = "member") Member member) {

        commentService.deleteLike(commentId, member);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }


}
