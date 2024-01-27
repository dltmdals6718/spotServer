package com.example.spotserver.controller;

import com.example.spotserver.domain.ErrorResponse;
import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.Role;
import com.example.spotserver.dto.request.SignInMember;
import com.example.spotserver.dto.request.SignUpMember;
import com.example.spotserver.exception.DuplicateException;
import com.example.spotserver.exception.ErrorCode;
import com.example.spotserver.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;


import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MemberControllerTest {

    private MockMvc mockMvc;

    private String signInUrl = "/members/signin";
    private String signUpUrl = "/members/signup";
    private String getMemberUrl = "/members";

    private SignUpMember signUpMember;
    private Member member;


    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MemberService memberService;

    @BeforeAll
    public void beforeAll() throws DuplicateException {
        System.out.println("MemberControllerTest.setup");
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilter(new CharacterEncodingFilter("utf-8", true))
                .build();

        signUpMember = new SignUpMember();
        signUpMember.setLoginId("존재아이디");
        signUpMember.setLoginPwd("비밀번호");
        signUpMember.setName("존재이름");
        member = memberService.addMember(signUpMember);
    }

    @AfterAll
    public void afterAll() {
        System.out.println("MemberControllerTest.afterAll");
        memberService.deleteMemberById(member.getId());
    }


    @Test
    @DisplayName("정상적인 회원가입")
    void signupMember() throws Exception {

        SignUpMember signUpMember = new SignUpMember();
        signUpMember.setName("새로운닉네임");
        signUpMember.setLoginId("새로운아이디");
        signUpMember.setLoginPwd("새로운비밀번호");

        ObjectMapper objectMapper = new ObjectMapper();
        String testSignUpBody = objectMapper.writeValueAsString(signUpMember);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, signUpUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(testSignUpBody)
                .characterEncoding("utf-8"));

        resultActions
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.memberId").exists(),
                        jsonPath("$.name").value(signUpMember.getName()),
                        jsonPath("$.role").value(Role.USER.name()))
                .andDo(print());

    }

    @Test
    @DisplayName("정상적인 로그인")
    void signinMember() throws Exception {

        SignInMember signInMember = new SignInMember();
        signInMember.setLoginId(signUpMember.getLoginId());
        signInMember.setLoginPwd(signUpMember.getLoginPwd());

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(signInMember);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, signInUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .characterEncoding("utf-8"));

        resultActions
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.expire_in").exists(),
                        jsonPath("$.token").exists())
                .andDo(print());


    }


    @Test
    @DisplayName("중복된 아이디 가입 시도")
    void duplicateIdSignup() throws Exception {

        SignUpMember duplicateIdMember = new SignUpMember();
        duplicateIdMember.setName("새로운이름");
        duplicateIdMember.setLoginId(signUpMember.getLoginId());
        duplicateIdMember.setLoginPwd("비밀번호");

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(duplicateIdMember);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, signUpUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .characterEncoding("utf-8"));

        resultActions
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.errorCode").value(ErrorCode.DUPLICATE_LOGINID.name()),
                        jsonPath("$.message").value(ErrorCode.DUPLICATE_LOGINID.getMessage()))
                .andDo(print());


    }

    @Test
    @DisplayName("중복된 이름 가입 시도")
    void duplicateSignup() throws Exception {

        SignUpMember duplicateNameMember = new SignUpMember();
        duplicateNameMember.setName(signUpMember.getName());
        duplicateNameMember.setLoginId("새로운아이디");
        duplicateNameMember.setLoginPwd("비밀번호");

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(duplicateNameMember);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, signUpUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .characterEncoding("utf-8"));

        resultActions
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.errorCode").value(ErrorCode.DUPLICATE_NAME.name()),
                        jsonPath("$.message").value(ErrorCode.DUPLICATE_NAME.getMessage()))
                .andDo(print());

    }

    @Test
    @DisplayName("회원정보 조회")
    void getMember() throws Exception {

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.GET, getMemberUrl+"/"+member.getId())
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
}