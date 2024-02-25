package com.example.spotserver.repository;

import com.example.spotserver.domain.Comment;
import com.example.spotserver.domain.CommentLike;
import com.example.spotserver.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsByCommentAndMember(Comment comment, Member member);
    Optional<CommentLike> findByCommentAndMember(Comment comment, Member member);
}
