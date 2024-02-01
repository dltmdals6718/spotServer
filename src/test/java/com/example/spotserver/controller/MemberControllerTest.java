package com.example.spotserver.controller;

import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.Role;
import com.example.spotserver.dto.request.SignInMember;
import com.example.spotserver.dto.request.SignUpMember;
import com.example.spotserver.dto.response.MemberResponse;
import com.example.spotserver.exception.DuplicateException;
import com.example.spotserver.exception.ErrorCode;
import com.example.spotserver.exception.LoginFailException;
import com.example.spotserver.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;


import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {MemberController.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MemberControllerTest {

    private MockMvc mockMvc;

    private String signInUrl = "/members/signin";
    private String signUpUrl = "/members/signup";
    private String getMemberUrl = "/members";

    @MockBean
    private MemberService memberService;

    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;

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

        SignUpMember signUpMember = new SignUpMember();
        signUpMember.setName("닉네임");
        signUpMember.setLoginId("아이디");
        signUpMember.setLoginPwd("비밀번호");

        MemberResponse memberResponse = new MemberResponse();
        memberResponse.setMemberId(1L);
        memberResponse.setName(signUpMember.getName());
        memberResponse.setRole(Role.USER);

        given(memberService.addMember(signUpMember))
                .willReturn(memberResponse);

        ObjectMapper objectMapper = new ObjectMapper();
        String testSignUpBody = objectMapper.writeValueAsString(signUpMember);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, signUpUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(testSignUpBody)
                .characterEncoding("utf-8")
                .with(csrf()));

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

        SignInMember signInMember = new SignInMember();
        signInMember.setLoginId("아이디");
        signInMember.setLoginPwd("비밀번호");

        Member member = new Member();
        member.setId(1L);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(signInMember);

        given(memberService.login(signInMember.getLoginId(), signInMember.getLoginPwd()))
                .willReturn(member);
        given(memberService.createToken(member.getId()))
                .willReturn("tokenString");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, signInUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .characterEncoding("utf-8")
                .with(csrf()));

        resultActions
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.expire_in").exists(),
                        jsonPath("$.token").exists())
                .andDo(print());


    }


    @Test
    @DisplayName("중복된 아이디 회원가입")
    void duplicateIdSignup() throws Exception {

        SignUpMember duplicateIdMember = new SignUpMember();
        duplicateIdMember.setName("이름");
        duplicateIdMember.setLoginId("중복 아이디");
        duplicateIdMember.setLoginPwd("비밀번호");

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(duplicateIdMember);

        given(memberService.addMember(duplicateIdMember))
                .willThrow(new DuplicateException(ErrorCode.DUPLICATE_LOGINID));

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, signUpUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .characterEncoding("utf-8")
                .with(csrf()));

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

        SignUpMember duplicateNameMember = new SignUpMember();
        duplicateNameMember.setName("중복된 이름");
        duplicateNameMember.setLoginId("아이디");
        duplicateNameMember.setLoginPwd("비밀번호");

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(duplicateNameMember);

        given(memberService.addMember(duplicateNameMember))
                .willThrow(new DuplicateException(ErrorCode.DUPLICATE_NAME));


        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, signUpUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .characterEncoding("utf-8")
                .with(csrf()));

        resultActions
                .andExpectAll(
                        status().is(ErrorCode.DUPLICATE_NAME.getHttpStatus().value()),
                        jsonPath("$.errorCode").value(ErrorCode.DUPLICATE_NAME.name()),
                        jsonPath("$.message").value(ErrorCode.DUPLICATE_NAME.getMessage()))
                .andDo(print());

    }

    @Test
    @DisplayName("회원정보 조회")
    void getMember() throws Exception {

        Member member = new Member();
        member.setId(1L);
        member.setName("name");
        member.setRole(Role.USER);

        given(memberService.getMember(1L))
                .willReturn(member);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.GET, getMemberUrl + "/" + 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8"));

        resultActions
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.memberId").value(member.getId()),
                        jsonPath("$.name").value(member.getName()),
                        jsonPath("$.role").value(member.getRole().name()))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 실패")
    void failLogin() throws Exception {

        SignInMember signInMember = new SignInMember();
        signInMember.setLoginId("아이디");
        signInMember.setLoginPwd("비밀번호");

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(signInMember);



        given(memberService.login(signInMember.getLoginId(), signInMember.getLoginPwd()))
                .willThrow(new LoginFailException(ErrorCode.FAIL_LOGIN));

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, signInUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .characterEncoding("utf-8")
                .with(csrf()));

        resultActions
                .andExpectAll(
                        status().is(ErrorCode.FAIL_LOGIN.getHttpStatus().value()),
                        jsonPath("$.errorCode").value(ErrorCode.FAIL_LOGIN.name()),
                        jsonPath("$.message").value(ErrorCode.FAIL_LOGIN.getMessage()))
                .andDo(print());


    }
}