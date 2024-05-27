package com.example.spotserver.controller;

import com.example.spotserver.config.auth.PrincipalDetails;
import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.Role;
import com.example.spotserver.dto.request.SignInMember;
import com.example.spotserver.dto.request.SignUpMember;
import com.example.spotserver.dto.response.MemberResponse;
import com.example.spotserver.exception.DuplicateException;
import com.example.spotserver.exception.ErrorCode;
import com.example.spotserver.exception.LoginFailException;
import com.example.spotserver.service.LocationService;
import com.example.spotserver.service.MemberService;
import com.example.spotserver.service.PosterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;


import java.nio.charset.StandardCharsets;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {MemberController.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MemberControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private PosterService posterService;

    @MockBean
    private LocationService locationService;

    @Autowired
    private WebApplicationContext context;

    @BeforeAll
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("utf-8", true))
                .build();

    }


    @Test
    @DisplayName("정상적인 회원가입")
    void signupMember() throws Exception {

        //given
        SignUpMember signUpMember = new SignUpMember();
        signUpMember.setName("닉네임");
        signUpMember.setLoginId("아이디");
        signUpMember.setLoginPwd("비밀번호");
        signUpMember.setMail("test@gmail.com");
        signUpMember.setCode(1234);

        MemberResponse memberResponse = new MemberResponse();
        memberResponse.setMemberId(1L);
        memberResponse.setName(signUpMember.getName());
        memberResponse.setRole(Role.USER);
        memberResponse.setMemberImg("...");


        ObjectMapper objectMapper = new ObjectMapper();
        String signUpMemberString = objectMapper.writeValueAsString(signUpMember);

        MockMultipartFile signUpMemberPart = new MockMultipartFile("signUpMember", null, MediaType.APPLICATION_JSON_VALUE, signUpMemberString.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile memberImg = new MockMultipartFile("memberImg", "test.jpeg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes());

        given(memberService.addMember(signUpMember, memberImg))
                .willReturn(memberResponse);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .multipart(HttpMethod.POST, "/members/signup")
                .file(signUpMemberPart)
                .file(memberImg));

        //then
        verify(memberService, times(1))
                .addMember(signUpMember, memberImg);
        resultActions
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.memberId").value(memberResponse.getMemberId()),
                        jsonPath("$.name").value(memberResponse.getName()),
                        jsonPath("$.role").value(memberResponse.getRole().name()))
                .andDo(print());

    }

    @Test
    @DisplayName("로그인 성공")
    void signinMember() throws Exception {

        //given
        SignInMember signInMember = new SignInMember();
        signInMember.setLoginId("아이디");
        signInMember.setLoginPwd("비밀번호");

        Member member = new Member();
        member.setId(1L);

        String token = "tokenString";

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(signInMember);

        MockHttpServletRequest request = new MockHttpServletRequest();

        given(memberService.login(signInMember.getLoginId(), signInMember.getLoginPwd(), request.getRemoteAddr()))
                .willReturn(member);
        given(memberService.createToken(member.getId()))
                .willReturn(token);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, "/members/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .characterEncoding("utf-8")
                .with(csrf()));

        //then
        resultActions
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.expire_in").exists(),
                        jsonPath("$.token").value(token))
                .andDo(print());
        verify(memberService, times(1))
                .login(signInMember.getLoginId(), signInMember.getLoginPwd(), request.getRemoteAddr());
        verify(memberService, times(1))
                .createToken(member.getId());


    }


    @Test
    @DisplayName("중복된 아이디 회원가입")
    void duplicateIdSignup() throws Exception {

        //given
        SignUpMember duplicateIdSignupMember = new SignUpMember();
        duplicateIdSignupMember.setName("이름");
        duplicateIdSignupMember.setLoginId("중복 아이디");
        duplicateIdSignupMember.setLoginPwd("비밀번호");
        duplicateIdSignupMember.setMail("test@gmail.com");
        duplicateIdSignupMember.setCode(1234);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(duplicateIdSignupMember);
        MockMultipartFile signUpMember = new MockMultipartFile("signUpMember", null, MediaType.APPLICATION_JSON_VALUE, body.getBytes());

        given(memberService.addMember(duplicateIdSignupMember, null))
                .willThrow(new DuplicateException(ErrorCode.DUPLICATE_LOGINID));

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .multipart(HttpMethod.POST, "/members/signup")
                .file(signUpMember));

        //then
        verify(memberService, times(1))
                .addMember(duplicateIdSignupMember, null);
        resultActions
                .andExpectAll(
                        status().is(ErrorCode.DUPLICATE_LOGINID.getHttpStatus().value()),
                        jsonPath("$.errorCode").value(ErrorCode.DUPLICATE_LOGINID.name()),
                        jsonPath("$.message").value(ErrorCode.DUPLICATE_LOGINID.getMessage()))
                .andDo(print());


    }

    @Test
    @DisplayName("중복된 이름 회원가입")
    void duplicateSignup() throws Exception {

        //given
        SignUpMember duplicateNameMember = new SignUpMember();
        duplicateNameMember.setName("중복된 이름");
        duplicateNameMember.setLoginId("아이디");
        duplicateNameMember.setLoginPwd("비밀번호");
        duplicateNameMember.setMail("test@gmail.com");
        duplicateNameMember.setCode(1234);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(duplicateNameMember);
        MockMultipartFile signUpMember = new MockMultipartFile("signUpMember", null, MediaType.APPLICATION_JSON_VALUE, body.getBytes());

        given(memberService.addMember(duplicateNameMember, null))
                .willThrow(new DuplicateException(ErrorCode.DUPLICATE_NAME));


        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .multipart(HttpMethod.POST, "/members/signup")
                .file(signUpMember));

        //then
        verify(memberService, times(1))
                .addMember(duplicateNameMember, null);
        resultActions
                .andExpectAll(
                        status().is(ErrorCode.DUPLICATE_NAME.getHttpStatus().value()),
                        jsonPath("$.errorCode").value(ErrorCode.DUPLICATE_NAME.name()),
                        jsonPath("$.message").value(ErrorCode.DUPLICATE_NAME.getMessage()))
                .andDo(print());

    }

    @Test
    @DisplayName("회원탈퇴")
    void deleteMember() throws Exception {

        //given
        Long memberId = 5L;
        Member member = new Member();
        member.setId(memberId);
        member.setName("홍길동");

        PrincipalDetails principalDetails = mock(PrincipalDetails.class);
        when(principalDetails.getMember())
                .thenReturn(member);

        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        doNothing()
                .when(memberService)
                .deleteMember(memberId);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.DELETE, "/members/" + memberId));

        //then
        verify(memberService, times(1))
                .deleteMember(memberId);
        resultActions
                .andExpectAll(status().isNoContent());

    }

    @Test
    @DisplayName("회원정보 조회")
    void getMember() throws Exception {

        //given
        MemberResponse memberResponse = new MemberResponse();
        memberResponse.setMemberId(1L);
        memberResponse.setName("테스터");
        memberResponse.setRole(Role.USER);
        memberResponse.setMemberImg("...");

        given(memberService.getMemberInfo(1L))
                .willReturn(memberResponse);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.GET, "/members/" + 1L)
                .characterEncoding("utf-8"));

        //then
        verify(memberService, times(1))
                .getMemberInfo(1L);
        resultActions
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.memberId").value(1L),
                        jsonPath("$.name").value("테스터"),
                        jsonPath("$.role").value("USER"),
                        jsonPath("$.memberImg").value("..."))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 실패")
    void failLogin() throws Exception {

        //given
        SignInMember signInMember = new SignInMember();
        signInMember.setLoginId("아이디");
        signInMember.setLoginPwd("비밀번호");

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(signInMember);

        MockHttpServletRequest request = new MockHttpServletRequest();

        given(memberService.login(signInMember.getLoginId(), signInMember.getLoginPwd(), request.getRemoteAddr()))
                .willThrow(new LoginFailException(ErrorCode.FAIL_LOGIN));

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, "/members/signin")
                .content(body.getBytes())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .with(csrf()));

        //then
        verify(memberService, times(1))
                .login(signInMember.getLoginId(), signInMember.getLoginPwd(), request.getRemoteAddr());
        resultActions
                .andExpectAll(
                        status().is(ErrorCode.FAIL_LOGIN.getHttpStatus().value()),
                        jsonPath("$.errorCode").value(ErrorCode.FAIL_LOGIN.name()),
                        jsonPath("$.message").value(ErrorCode.FAIL_LOGIN.getMessage()))
                .andDo(print());


    }
}