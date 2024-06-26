package com.example.spotserver.controller;

import com.example.spotserver.config.auth.PrincipalDetails;
import com.example.spotserver.config.jwt.JwtProperties;
import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.Role;
import com.example.spotserver.dto.request.LogoutRequest;
import com.example.spotserver.dto.request.RefreshRequest;
import com.example.spotserver.dto.request.SignInMember;
import com.example.spotserver.dto.request.SignUpMember;
import com.example.spotserver.dto.response.AccessTokenResponse;
import com.example.spotserver.dto.response.MemberResponse;
import com.example.spotserver.dto.response.TokenResponse;
import com.example.spotserver.exception.AuthenticationException;
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
import java.util.UUID;

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

        TokenResponse tokenResponse = new TokenResponse();
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        tokenResponse.setAccessToken(accessToken);
        tokenResponse.setAccessExpireIn(1L);
        tokenResponse.setRefreshToken(refreshToken);
        tokenResponse.setRefreshExpireIn(2L);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(signInMember);

        MockHttpServletRequest request = new MockHttpServletRequest();

        given(memberService.login(signInMember.getLoginId(), signInMember.getLoginPwd(), request.getRemoteAddr()))
                .willReturn(member);
        given(memberService.createToken(member.getId()))
                .willReturn(tokenResponse);

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
                        jsonPath("$.accessToken").value(accessToken),
                        jsonPath("$.accessExpireIn").value(tokenResponse.getAccessExpireIn()),
                        jsonPath("$.refreshToken").value(refreshToken),
                        jsonPath("$.refreshExpireIn").value(tokenResponse.getRefreshExpireIn()))
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

    @Test
    @DisplayName("액세스 토큰 갱신")
    void refreshToken() throws Exception {

        //given
        String resfreshToken = UUID.randomUUID().toString();
        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken(resfreshToken);

        String accessToken = UUID.randomUUID().toString();
        Long expireTime = JwtProperties.ACCESS_TOKEN_EXPIRE_TIME;
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setAccessToken(accessToken);
        accessTokenResponse.setAccessExpireIn(expireTime);

        given(memberService.refreshToken(refreshRequest))
                .willReturn(accessTokenResponse);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, "/members/refresh")
                .content(new ObjectMapper().writeValueAsString(refreshRequest))
                .contentType(MediaType.APPLICATION_JSON));

        //then
        verify(memberService, times(1))
                .refreshToken(refreshRequest);

        resultActions
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.accessToken").value(accessToken),
                        jsonPath("$.accessExpireIn").value(expireTime)
                )
                .andDo(print());
    }

    @Test
    @DisplayName("만료된 refreshToken으로 갱신 시도")
    void expiredRefreshToken() throws Exception {

        //given
        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken(UUID.randomUUID().toString());

        given(memberService.refreshToken(refreshRequest))
                .willThrow(new AuthenticationException(ErrorCode.JWT_EXPIRED_TOKEN));

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, "/members/refresh")
                .content(new ObjectMapper().writeValueAsBytes(refreshRequest))
                .contentType(MediaType.APPLICATION_JSON));

        //then
        verify(memberService, times(1))
                .refreshToken(refreshRequest);

        resultActions
                .andExpectAll(
                        status().is(ErrorCode.JWT_EXPIRED_TOKEN.getHttpStatus().value()),
                        jsonPath("$.errorCode").value(ErrorCode.JWT_EXPIRED_TOKEN.name()),
                        jsonPath("$.message").value(ErrorCode.JWT_EXPIRED_TOKEN.getMessage())
                )
                .andDo(print());
    }

    @Test
    @DisplayName("로그아웃")
    void logout() throws Exception {

        //given
        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setRefreshToken("RefreshToken");

        doNothing()
                .when(memberService)
                .logout(logoutRequest);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, "/members/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(logoutRequest)));

        //then
        verify(memberService, times(1))
                .logout(logoutRequest);

        resultActions
                .andExpectAll(
                        status().isNoContent())
                .andDo(print());
    }

    @Test
    @DisplayName("중복된 로그아웃 시도")
    void duplicateLogout() throws Exception {

        //given
        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setRefreshToken("RefreshToken");

        doThrow(new AuthenticationException(ErrorCode.JWT_LOGOUT_TOKEN))
                .when(memberService).logout(logoutRequest);

        //when & then
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, "/members/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(logoutRequest)));

        resultActions
                .andExpectAll(
                        jsonPath("$.errorCode").value(ErrorCode.JWT_LOGOUT_TOKEN.name()),
                        jsonPath("$.message").value(ErrorCode.JWT_LOGOUT_TOKEN.getMessage())
                )
                .andDo(print());
    }

}