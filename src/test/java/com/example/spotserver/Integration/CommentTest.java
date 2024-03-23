package com.example.spotserver.Integration;

import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.CommentRequest;
import com.example.spotserver.exception.DuplicateException;
import com.example.spotserver.exception.ErrorCode;
import com.example.spotserver.exception.PermissionException;
import com.example.spotserver.repository.*;
import com.example.spotserver.service.CommentService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.NoSuchElementException;

@SpringBootTest
@Transactional
public class CommentTest {

    @Autowired
    CommentService commentService;


    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PosterRepository posterRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private EntityManager em;

    Member member;
    Location location;
    Poster poster;
    Comment comment;


    @BeforeEach
    void init() {

        member = new Member();
        member.setName("테스터");
        member.setRole(Role.USER);
        memberRepository.save(member);

        location = new Location();
        location.setTitle("테스트 장소");
        locationRepository.save(location);

        poster = new Poster();
        poster.setTitle("테스트 게시글");
        poster.setWriter(member);
        poster.setLocation(location);
        posterRepository.save(poster);

        comment = new Comment();
        comment.setContent("테스트 댓글");
        comment.setPoster(poster);
        comment.setWriter(member);
        commentRepository.save(comment);

        CommentLike commentLike = new CommentLike();
        commentLike.setComment(comment);
        commentLike.setMember(member);
        commentLikeRepository.save(commentLike);

    }


    @Test
    @DisplayName("댓글 작성")
    void addComment() {

        //given
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setContent("댓글 내용");
        Comment comment = CommentRequest.toEntity(commentRequest);

        //when
        commentService.addComment(poster.getId(), comment, member.getId());
        em.flush();
        em.clear();

        //then
        Comment findComment = commentRepository.findById(comment.getId())
                .orElseThrow(() -> new NoSuchElementException());

        Assertions
                .assertThat(findComment.getWriter().getId())
                .isEqualTo(member.getId());

        Assertions
                .assertThat(findComment.getContent())
                .isEqualTo(comment.getContent());
    }

    @Test
    @DisplayName("댓글 삭제")
    void deleteComment() throws PermissionException {

        //given & when
        em.clear();
        commentService.deleteComment(comment.getId(), member.getId());

        //then
        em.flush();
        Assertions
                .assertThat(commentRepository.findById(comment.getId()))
                .isNotPresent();
    }

    @Test
    @DisplayName("좋아요 등록")
    void addLike() throws DuplicateException {

        //given
        Member testMember = new Member();
        testMember.setName("좋아요 누를 예정인 사람");
        memberRepository.save(testMember);

        //when
        commentService.addLike(comment.getId(), testMember.getId());

        //then
        Assertions
                .assertThat(commentLikeRepository.findByCommentAndMember(comment, testMember))
                .isPresent();

    }

    @Test
    @DisplayName("중복된 좋아요 등록")
    void addDuplicateLike() throws DuplicateException {
        Assertions
                .assertThatThrownBy(() -> commentService.addLike(comment.getId(), member.getId()))
                .isInstanceOf(DuplicateException.class)
                .hasMessage(ErrorCode.DUPLICATE_LIKE.getMessage());

    }

    @Test
    @DisplayName("좋아요 취소")
    void deleteLike() {

        //given & when
        commentService.deleteLike(comment.getId(), member.getId());

        //then
        Assertions
                .assertThat(commentLikeRepository.findByCommentAndMember(comment, member))
                .isNotPresent();

    }

    @Test
    @DisplayName("존재하지 않는 좋아요 취소")
    void test() {

        //given
        Member testMember = new Member();
        testMember.setName("좋아요 누른적 없는 사람");
        memberRepository.save(testMember);

        //when & then
        Assertions
                .assertThatThrownBy(() -> commentService.deleteLike(comment.getId(), testMember.getId()))
                .isInstanceOf(NoSuchElementException.class);
    }

}
