package com.example.spotserver.controller;

import com.example.spotserver.config.CorsConfig;
import com.example.spotserver.config.SecurityConfig;
import com.example.spotserver.config.auth.PrincipalDetails;
import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.Role;
import com.example.spotserver.dto.request.CommentRequest;
import com.example.spotserver.dto.response.CommentResponse;
import com.example.spotserver.repository.MemberRepository;
import com.example.spotserver.service.CommentService;
import com.example.spotserver.service.PosterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
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

    @MockBean
    private PosterService posterService;

    private Member member;
    private PrincipalDetails principalDetails;


    @BeforeAll
    public void beforeAll() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilter(new CharacterEncodingFilter("utf-8", true))
                .build();

        SecurityContext context = SecurityContextHolder.getContext();

        member = mock(Member.class);
        principalDetails = mock(PrincipalDetails.class);
        when(member.getId())
                .thenReturn(1L);
        when(member.getName())
                .thenReturn("name");
        when(principalDetails.getMember())
                .thenReturn(member);

        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        context.setAuthentication(authentication);

    }

    @BeforeEach
    public void beforeEach() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        context.setAuthentication(authentication);
    }

//    @Test
    @DisplayName(value = "댓글 작성")
    void addComment() throws Exception {
//
//        Long posterId = 2L;
//        Long commentId = 3L;
//
//        String writeCommnetUrl = "/comments/" + posterId;
//        CommentRequest commentRequest = new CommentRequest();
//        commentRequest.setContent("테스트 댓글 내용");
//
//        CommentResponse commentResponse = new CommentResponse();
//        commentResponse.setCommentId(commentId);
//        commentResponse.setWriterId(member.getId());
//        commentResponse.setWriterName(member.getName());
//        commentResponse.setContent(commentRequest.getContent());
//        commentResponse.setRegDate(LocalDateTime.now());
//
//        given(commentService.addComment(posterId, commentRequest, member))
//                .willReturn(commentResponse);
//
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        String body = objectMapper.writeValueAsString(commentRequest);
//
//        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
//                .request(HttpMethod.POST, writeCommnetUrl)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(body)
//                .characterEncoding("utf-8")
//                .with(csrf()));
//
//        verify(commentService, times(1))
//                .addComment(posterId, commentRequest, member);
//
//
//        resultActions
//                .andExpectAll(
//                        status().is(HttpStatus.CREATED.value()),
//                        jsonPath("$.commentId").value(commentId),
//                        jsonPath("$.writerId").value(member.getId()),
//                        jsonPath("$.content").value(commentRequest.getContent()),
//                        jsonPath("$.regDate").value(commentResponse.getRegDate().toString()))
//                .andDo(print());


    }

//    @Test
    @DisplayName(value = "특정 댓글 조회")
    void getComment() throws Exception {

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.GET, "")
                .characterEncoding("utf-8")
                .with(csrf()));

        resultActions
                .andDo(print());
    }

    @Test
    @DisplayName(value = "전체 댓글 조회")
    void getComments() {
    }

    @Test
    @DisplayName(value = "댓글 삭제")
    void deleteComment() {
    }

    @Test
    @DisplayName(value = "댓글 수정")
    void updateComment() {
    }


}