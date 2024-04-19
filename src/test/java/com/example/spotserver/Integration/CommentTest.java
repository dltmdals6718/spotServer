package com.example.spotserver.Integration;

import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.CommentConditionRequest;
import com.example.spotserver.dto.request.CommentRequest;
import com.example.spotserver.dto.response.CommentResponse;
import com.example.spotserver.dto.response.PageResponse;
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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@SpringBootTest
@Transactional
public class CommentTest {

    @Autowired
    private CommentService commentService;

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

    @Test
    @DisplayName("댓글 작성")
    void addComment() {

        //given
        Location location = new Location();
        location.setTitle("장소명");
        locationRepository.save(location);

        Member member = new Member();
        member.setName("회원A");
        memberRepository.save(member);

        Poster poster = new Poster();
        poster.setTitle("게시글 제목");
        poster.setLocation(location);
        poster.setWriter(member);
        posterRepository.save(poster);

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
    @DisplayName("댓글 수정")
    void updateComment() throws PermissionException {

        //given
        Member member = new Member();
        member.setName("회원A");
        memberRepository.save(member);

        Comment beforeComment = new Comment();
        beforeComment.setWriter(member);
        beforeComment.setContent("수정전");
        commentRepository.save(beforeComment);

        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setContent("수정후");

        //when
        commentService.updateComment(beforeComment.getId(), commentRequest, member.getId());
        em.flush();
        em.clear();

        //then
        Comment afterComment = commentRepository.findById(beforeComment.getId())
                .orElseThrow(() -> new NoSuchElementException());
        Assertions
                .assertThat(afterComment.getContent())
                .isEqualTo(commentRequest.getContent());
    }

    @Test
    @DisplayName("댓글 삭제")
    void deleteComment() throws PermissionException {

        //given
        Location location = new Location();
        location.setTitle("장소명");
        locationRepository.save(location);

        Member member = new Member();
        member.setName("회원A");
        memberRepository.save(member);

        Poster poster = new Poster();
        poster.setTitle("게시글 제목");
        poster.setLocation(location);
        poster.setWriter(member);
        posterRepository.save(poster);

        Comment comment = new Comment();
        comment.setPoster(poster);
        comment.setWriter(member);
        commentRepository.save(comment);

        //when
        em.clear();
        commentService.deleteComment(comment.getId(), member.getId());

        //then
        Optional<Comment> findComment = commentRepository.findById(comment.getId());
        Assertions
                .assertThat(findComment)
                .isNotPresent();
    }

    @Test
    @DisplayName("좋아요 등록")
    void addLike() throws DuplicateException {

        //given
        Location location = new Location();
        location.setTitle("장소명");
        locationRepository.save(location);

        Member member = new Member();
        member.setName("회원A");
        memberRepository.save(member);

        Poster poster = new Poster();
        poster.setTitle("게시글 제목");
        poster.setLocation(location);
        poster.setWriter(member);
        posterRepository.save(poster);

        Comment comment = new Comment();
        comment.setPoster(poster);
        comment.setWriter(member);
        commentRepository.save(comment);

        //when
        commentService.addLike(comment.getId(), member.getId());

        //then
        Optional<CommentLike> commentLike = commentLikeRepository.findByCommentAndMember(comment, member);
        Assertions
                .assertThat(commentLike)
                .isPresent();

    }

    @Test
    @DisplayName("중복된 좋아요 등록")
    void addDuplicateLike() {
        //given
        Location location = new Location();
        location.setTitle("장소명");
        locationRepository.save(location);

        Member member = new Member();
        member.setName("회원A");
        memberRepository.save(member);

        Poster poster = new Poster();
        poster.setTitle("게시글 제목");
        poster.setLocation(location);
        poster.setWriter(member);
        posterRepository.save(poster);

        Comment comment = new Comment();
        comment.setPoster(poster);
        comment.setWriter(member);
        commentRepository.save(comment);

        CommentLike commentLike = new CommentLike();
        commentLike.setMember(member);
        commentLike.setComment(comment);
        commentLikeRepository.save(commentLike);

        //when & then
        Assertions
                .assertThatThrownBy(() -> commentService.addLike(comment.getId(), member.getId()))
                .isInstanceOf(DuplicateException.class)
                .hasMessage(ErrorCode.DUPLICATE_LIKE.getMessage());

    }

    @Test
    @DisplayName("좋아요 취소")
    void deleteLike() {

        //given
        Location location = new Location();
        location.setTitle("장소명");
        locationRepository.save(location);

        Member member = new Member();
        member.setName("회원A");
        memberRepository.save(member);

        Poster poster = new Poster();
        poster.setTitle("게시글 제목");
        poster.setLocation(location);
        poster.setWriter(member);
        posterRepository.save(poster);

        Comment comment = new Comment();
        comment.setPoster(poster);
        comment.setWriter(member);
        commentRepository.save(comment);

        CommentLike commentLike = new CommentLike();
        commentLike.setComment(comment);
        commentLike.setMember(member);
        commentLikeRepository.save(commentLike);

        //when
        commentService.deleteLike(comment.getId(), member.getId());

        //then
        Optional<CommentLike> findCommentLike = commentLikeRepository.findByCommentAndMember(comment, member);
        Assertions
                .assertThat(findCommentLike)
                .isNotPresent();

    }

    @Test
    @DisplayName("존재하지 않는 좋아요 취소")
    void NoExistLikeDelete() {

        //given
        Location location = new Location();
        location.setTitle("장소명");
        locationRepository.save(location);

        Member member = new Member();
        member.setName("회원A");
        memberRepository.save(member);

        Poster poster = new Poster();
        poster.setTitle("게시글 제목");
        poster.setLocation(location);
        poster.setWriter(member);
        posterRepository.save(poster);

        Comment comment = new Comment();
        comment.setPoster(poster);
        comment.setWriter(member);
        commentRepository.save(comment);

        //when & then
        Assertions
                .assertThatThrownBy(() -> commentService.deleteLike(comment.getId(), member.getId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("댓글 최신순 조회")
    void getComments() {

        //given
        Member writer = new Member();
        writer.setName("홍길동");
        memberRepository.save(writer);

        Poster locationPoster = new Poster();
        locationPoster.setWriter(writer);
        posterRepository.save(locationPoster);

        Long commentCnt = 6L;

        for(int i=1; i<=commentCnt; i++) {
            Comment posterComment = new Comment();
            posterComment.setPoster(locationPoster);
            posterComment.setWriter(writer);
            posterComment.setContent(i + "번째 댓글");
            commentRepository.save(posterComment);
        }

        Integer page = 2;
        Integer size = 5;
        CommentConditionRequest commentConditionRequest = new CommentConditionRequest();
        commentConditionRequest.setPage(page);
        commentConditionRequest.setSize(size);
        commentConditionRequest.setSort("recent");

        em.flush();

        //when
        PageResponse<CommentResponse> pageResponse = commentService.getComments(locationPoster.getId(), commentConditionRequest);
        List<CommentResponse> comments = pageResponse.getResults();
        PageInfo pageInfo = pageResponse.getPageInfo();

        //then
        Assertions
                .assertThat(pageInfo.getNumberOfElements())
                .isEqualTo(comments.size());
        Assertions
                .assertThat(pageInfo.getTotalElements())
                .isEqualTo(commentCnt);
        Assertions
                .assertThat(pageInfo.getPage())
                .isEqualTo(page);
        Assertions
                .assertThat(pageInfo.getSize())
                .isEqualTo(size);

        Assertions
                .assertThat(comments.get(0).getContent())
                .isEqualTo(commentCnt + "번째 댓글");
    }
}
