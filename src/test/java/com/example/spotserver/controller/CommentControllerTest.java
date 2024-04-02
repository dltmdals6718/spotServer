package com.example.spotserver.controller;

import com.example.spotserver.config.auth.PrincipalDetails;
import com.example.spotserver.domain.Comment;
import com.example.spotserver.domain.Member;
import com.example.spotserver.dto.request.CommentRequest;
import com.example.spotserver.dto.response.CommentResponse;
import com.example.spotserver.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(value = {CommentController.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommentControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    private Member member;
    private PrincipalDetails principalDetails;


    @BeforeAll
    public void beforeAll() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilter(new CharacterEncodingFilter("utf-8", true))
                .build();

        member = new Member();
        member.setId(1L);
        member.setName("닉네임");
        principalDetails = mock(PrincipalDetails.class);

        when(principalDetails.getMember())
                .thenReturn(member);

    }

    @BeforeEach
    public void beforeEach() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        context.setAuthentication(authentication);
    }

    @Test
    @DisplayName(value = "댓글 작성")
    void addComment() throws Exception {

        //given
        Long posterId = 2L;

        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setContent("테스트 댓글 내용");
        Comment comment = CommentRequest.toEntity(commentRequest);

        given(commentService.addComment(posterId, comment, member.getId()))
                .willReturn(1L);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(commentRequest);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, "/posters/" + posterId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .characterEncoding("utf-8")
                .with(csrf()));

        //then
        verify(commentService, times(1))
                .addComment(posterId, comment, member.getId());
        resultActions
                .andExpectAll(
                        status().is(HttpStatus.CREATED.value()),
                        jsonPath("$.commentId").value(1L))
                .andDo(print());

    }

    @Test
    @DisplayName(value = "특정 댓글 조회")
    void getComment() throws Exception {

        //given
        Long commentId = 2L;
        Long likeCnt = 3L;
        Long writerId = 5L;
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setCommentId(commentId);
        commentResponse.setWriterId(writerId);
        commentResponse.setWriterName("작성자");
        commentResponse.setRegDate(LocalDateTime.now());
        commentResponse.setContent("댓글 내용");
        commentResponse.setLikeCnt(likeCnt);
        commentResponse.setMemberImg("");

        given(commentService.getComment(commentId))
                .willReturn(commentResponse);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.GET, "/comments/" + commentId)
                .characterEncoding("utf-8")
                .with(csrf()));

        //then
        verify(commentService, times(1))
                .getComment(commentId);
        resultActions
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.commentId").value(commentResponse.getCommentId()),
                        jsonPath("$.writerId").value(commentResponse.getWriterId()),
                        jsonPath("$.writerName").value(commentResponse.getWriterName()),
                        jsonPath("$.memberImg").value(commentResponse.getMemberImg()),
                        jsonPath("$.content").value(commentResponse.getContent()),
                        jsonPath("$.regDate").value(commentResponse.getRegDate().toString()),
                        jsonPath("$.likeCnt").value(commentResponse.getLikeCnt()))
                .andDo(print());
    }

    @Test
    @DisplayName(value = "댓글 삭제")
    void deleteComment() throws Exception {

        //given
        Long commendId = 1L;

        doNothing()
                .when(commentService)
                .deleteComment(commendId, 1L);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.DELETE, "/comments/" + commendId));

        //then
        verify(commentService, times(1))
                .deleteComment(commendId, 1L);
        resultActions
                .andExpectAll(
                        status().is(HttpStatus.NO_CONTENT.value()))
                .andDo(print());
    }

    @Test
    @DisplayName(value = "댓글 수정")
    void updateComment() throws Exception {

        //given
        Long commentId = 1L;
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setContent("수정될 댓글 내용");

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(commentRequest);


        doNothing()
                .when(commentService)
                .updateComment(commentId,  commentRequest, 1L);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.PUT, "/comments/" + commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        //then
        verify(commentService, times(1))
                .updateComment(commentId, commentRequest, 1L);
        resultActions
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.commentId").value(commentId))
                .andDo(print());
    }

    @Test
    @DisplayName(value = "댓글 좋아요 등록")
    void addLike() throws Exception {

        //given
        Long commentId = 2L;
        doNothing()
                .when(commentService)
                .addLike(commentId, 1L);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, "/comments/" + commentId + "/likes"));

        //then
        verify(commentService, times(1))
                .addLike(commentId, 1L);
        resultActions
                .andExpectAll(
                        status().is(HttpStatus.CREATED.value()))
                .andDo(print());
    }

    @Test
    @DisplayName(value = "댓글 좋아요 취소")
    void deleteLike() throws Exception {

        //given
        Long commentId = 2L;
        doNothing()
                .when(commentService)
                .deleteLike(commentId, 1L);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.DELETE, "/comments/" + commentId + "/likes"));

        //then
        verify(commentService, times(1))
                .deleteLike(commentId, 1L);
        resultActions
                .andExpectAll(status().is(HttpStatus.NO_CONTENT.value()))
                .andDo(print());
    }


}