package com.example.spotserver.Integration;

import com.amazonaws.services.s3.AmazonS3Client;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.spotserver.config.jwt.JwtProperties;
import com.example.spotserver.domain.ImageStore;
import com.example.spotserver.domain.MailCertification;
import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.MemberImage;
import com.example.spotserver.dto.request.LogoutRequest;
import com.example.spotserver.dto.request.MemberUpdateRequest;
import com.example.spotserver.dto.request.RefreshRequest;
import com.example.spotserver.dto.request.SignUpMember;
import com.example.spotserver.dto.response.AccessTokenResponse;
import com.example.spotserver.dto.response.TokenResponse;
import com.example.spotserver.exception.*;
import com.example.spotserver.repository.MailCertificationRepository;
import com.example.spotserver.repository.MemberImageRepository;
import com.example.spotserver.repository.MemberRepository;
import com.example.spotserver.service.*;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Random;

@SpringBootTest
@Transactional
public class MemberTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MailSerivce mailSerivce;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberImageRepository memberImageRepository;

    @Autowired
    private MailCertificationRepository mailCertificationRepository;

    @Autowired
    private ImageStore imageStore;

    @Autowired
    private AmazonS3Client amazonS3Client;

    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("회원가입")
    void signup() throws FileException, DuplicateException, IOException, MailException {

        //given
        MailCertification mailCertification = new MailCertification();
        String mail = "test@mail.com";
        Integer code = 1234;
        mailCertification.setCode(code);
        mailCertification.setMail(mail);

        SignUpMember signUpMember = new SignUpMember();
        String loginId = "테스트 아이디";
        String loginPwd = "테스트 비밀번호";
        signUpMember.setLoginId(loginId);
        signUpMember.setLoginPwd(loginPwd);
        signUpMember.setMail(mail);
        signUpMember.setCode(code);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("profileImg", "profile.jpg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes());

        mailCertificationRepository.save(mailCertification);

        //when
        memberService.addMember(signUpMember, mockMultipartFile);

        //then
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new NoSuchElementException());

        Assertions
                .assertThat(member.getLoginId())
                .isEqualTo(loginId);
        Assertions
                .assertThat(member.getMail())
                .isEqualTo(mail);
        Assertions
                .assertThat(member.getMemberImg().getUploadFileName())
                .isEqualTo("profile.jpg");

        imageStore.deleteMemberImage(member.getMemberImg());
    }

    @Test
    @DisplayName("중복된 닉네임으로 가입 시도")
    void duplicateNameSignup() {

        //given
        Member member = new Member();
        member.setName("홍길동");
        memberRepository.save(member);
        em.flush();

        MailCertification mailCertification = new MailCertification();
        mailCertification.setMail("test@mail.com");
        mailCertification.setCode(12345);
        mailCertificationRepository.save(mailCertification);

        SignUpMember signUpMember = new SignUpMember();
        signUpMember.setLoginId("중복 아닌 아이디");
        signUpMember.setLoginPwd("비밀번호");
        signUpMember.setMail("test@mail.com");
        signUpMember.setName("홍길동");
        signUpMember.setCode(12345);

        //when & then
        Assertions
                .assertThatThrownBy(() -> memberService.addMember(signUpMember, null))
                .isInstanceOf(DuplicateException.class)
                .hasMessage(ErrorCode.DUPLICATE_NAME.getMessage());
    }

    @Test
    @DisplayName("중복된 아이디로 가입 시도")
    void duplicateLoginIdSignup() {
        //given
        Member member = new Member();
        member.setLoginId("loginId");
        memberRepository.save(member);
        em.flush();

        Integer code = 12345;
        MailCertification mailCertification = new MailCertification();
        mailCertification.setMail("test@mail.com");
        mailCertification.setCode(code);
        mailCertificationRepository.save(mailCertification);

        SignUpMember signUpMember = new SignUpMember();
        signUpMember.setLoginId("loginId");
        signUpMember.setLoginPwd("비밀번호");
        signUpMember.setMail("test@mail.com");
        signUpMember.setName("닉네임");
        signUpMember.setCode(code);

        //when & then
        Assertions
                .assertThatThrownBy(() -> memberService.addMember(signUpMember, null))
                .isInstanceOf(DuplicateException.class)
                .hasMessage(ErrorCode.DUPLICATE_LOGINID.getMessage());
    }

    @Test
    @DisplayName("로그인")
    void signin() throws LoginFailException {

        MockHttpServletRequest request = new MockHttpServletRequest();

        Member member = new Member();
        member.setLoginId("loginId");
        member.setLoginPwd(new BCryptPasswordEncoder().encode("loginPwd"));
        memberRepository.save(member);
        em.flush();

        Member loginMember = memberService.login("loginId", "loginPwd", request.getRemoteAddr());
        Assertions
                .assertThat(member.getId())
                .isEqualTo(loginMember.getId());
    }

    @Test
    @DisplayName("로그인 실패")
    void failSignin() {

        MockHttpServletRequest request = new MockHttpServletRequest();

        Member member = new Member();
        member.setLoginId("loginId");
        member.setLoginPwd("loginPwd");

        Assertions
                .assertThatThrownBy(() -> memberService.login(member.getLoginId(), "wrongPwd", request.getRemoteAddr()))
                .isInstanceOf(LoginFailException.class)
                .hasMessage(ErrorCode.FAIL_LOGIN.getMessage());
    }

    @Test
    @DisplayName("회원 정보 및 프로필 사진 변경")
    void updateMember() throws DuplicateException, IOException, FileException {

        //given
        Member member = new Member();
        member.setName("기존 이름");
        memberRepository.save(member);

        MemberImage memberImage = imageStore.storeMemberImage(new MockMultipartFile("memberImg", "upload.jpg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes()));
        memberImage.setMember(member);
        memberImageRepository.save(memberImage);
        em.flush();

        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest();
        memberUpdateRequest.setName("updateName");
        String uploadFileName = "updateImg.jpg";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("memberImg", uploadFileName, MediaType.IMAGE_JPEG_VALUE, "img".getBytes());

        //when
        em.clear();
        memberService.updateMember(memberUpdateRequest, mockMultipartFile, member.getId());

        //then
        Member updateMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new NoSuchElementException());

        Assertions
                .assertThat(updateMember.getName())
                .isEqualTo(memberUpdateRequest.getName());
        Assertions
                .assertThat(updateMember.getMemberImg().getUploadFileName())
                .isEqualTo(uploadFileName);
        Assertions
                .assertThat(amazonS3Client.doesObjectExist(imageStore.getBucket(), imageStore.getMemberImgDir() + memberImage.getStoreFileName()))
                .isFalse();
        Assertions
                .assertThat(amazonS3Client.doesObjectExist(imageStore.getBucket(), imageStore.getMemberImgDir() + updateMember.getMemberImg().getStoreFileName()))
                .isTrue();

        imageStore.deleteMemberImage(updateMember.getMemberImg());
    }

    @Test
    @DisplayName("회원 탈퇴")
    void deleteMember() throws IOException {

        //given
        Member member = new Member();
        member.setLoginId("loginId");
        member.setLoginPwd(new BCryptPasswordEncoder().encode("loginPwd"));

        MockMultipartFile profileImg = new MockMultipartFile("profileImg", "myProfile.jpg", MediaType.IMAGE_JPEG_VALUE, "".getBytes());
        MemberImage memberImage = imageStore.storeMemberImage(profileImg);
        memberImage.setMember(member);
        member.setMemberImg(memberImage);
        memberRepository.save(member);
        em.flush();

        //when
        em.clear();
        memberService.deleteMember(member.getId());

        //then
        Assertions
                .assertThat(memberRepository.findById(member.getId()))
                .isNotPresent();
        Assertions
                .assertThat(memberImageRepository.findById(memberImage.getId()))
                .isNotPresent();
        Assertions
                .assertThat(amazonS3Client.doesObjectExist(imageStore.getBucket(), imageStore.getMemberImgDir() + memberImage.getStoreFileName()))
                .isFalse();
    }

    @Test
    @DisplayName("이메일 인증 번호 전송")
    void mailCertification() throws MessagingException, MailException {
        //given & when
        mailSerivce.sendMailCertification("test@mail.com");

        //then
        Assertions
                .assertThat(mailCertificationRepository.findByMail("test@mail.com"))
                .isPresent();
    }

    @Test
    @DisplayName("5분내 이메일 인증 번호 재요청 에러")
    void failMailRequestInTime() throws MessagingException, MailException {

        //given
        mailSerivce.sendMailCertification("test@mail.com");

        //when & then
        Assertions
                .assertThatThrownBy(() -> mailSerivce.sendMailCertification("test@mail.com"))
                .hasMessage(ErrorCode.FAIL_MAIL_CERTIFICATION_REQUEST.getMessage());
    }

    @Test
    @DisplayName("이메일 인증 미요청")
    void notMailCertificationRequest() {

        //given
        SignUpMember signUpMember = new SignUpMember();
        signUpMember.setLoginId("중복 아닌 아이디");
        signUpMember.setMail("test@mail.com");
        signUpMember.setName("중복 아닌 닉네임");

        //when & then
        Assertions
                .assertThatThrownBy(() -> memberService.addMember(signUpMember, null))
                .isInstanceOf(MailException.class)
                .hasMessage(ErrorCode.NOT_MAIL_CERTIFICATION.getMessage());

    }

    @Test
    @DisplayName("이메일 인증 번호 미일치")
    void notMatchMailCertificationCode() {

        //given
        Integer code = 12345;
        MailCertification mailCertification = new MailCertification();
        mailCertification.setMail("test@mail.com");
        mailCertification.setCode(code);
        mailCertificationRepository.save(mailCertification);

        SignUpMember signUpMember = new SignUpMember();
        signUpMember.setLoginId("아이디");
        signUpMember.setLoginPwd("비밀번호");
        signUpMember.setMail("test@mail.com");
        signUpMember.setName("닉네임");
        signUpMember.setCode(123456789);

        //when & then
        Assertions
                .assertThatThrownBy(() -> memberService.addMember(signUpMember, null))
                .isInstanceOf(MailException.class)
                .hasMessage(ErrorCode.FAIL_MAIL_CERTIFICATION.getMessage());

    }

    @Test
    @DisplayName("이메일 인증 제한 시간 초과")
    void failMailCertificationInTime() {

        //given
        Integer code = 12345;
        MailCertification mailCertification = new MailCertification();
        mailCertification.setMail("test@mail.com");
        mailCertification.setCode(code);
        mailCertificationRepository.save(mailCertification);

        MailCertification findMailCertification = mailCertificationRepository.findByMail("test@mail.com")
                .orElseThrow(() -> new NoSuchElementException());
        findMailCertification.setRegDate(LocalDateTime.now().minusDays(1));

        SignUpMember signUpMember = new SignUpMember();
        signUpMember.setLoginId("중복 아닌 아이디");
        signUpMember.setLoginPwd("비밀번호");
        signUpMember.setMail("test@mail.com");
        signUpMember.setName("중복 아닌 닉네임");
        signUpMember.setCode(code);

        //when & then
        Assertions
                .assertThatThrownBy(() -> memberService.addMember(signUpMember, null))
                .isInstanceOf(MailException.class)
                .hasMessage(ErrorCode.FAIL_MAIL_TIMEOUT.getMessage());

    }

    @Test
    @DisplayName("리프레쉬 토큰 발급")
    void createRefreshToken() {

        //given
        Long memberId = new Random().nextLong();

        //when
        TokenResponse tokenResponse = memberService.createToken(memberId);

        //then
        String refreshToken = tokenResponse.getRefreshToken();
        Assertions
                .assertThat(redisTemplate.hasKey(refreshToken))
                .isTrue();

        //clear
        redisTemplate.delete(refreshToken);
    }

    @Test
    @DisplayName("액세스 토큰 갱신")
    void refreshToken() throws AuthenticationException {

        //given
        Long memberId = new Random().nextLong();
        TokenResponse tokenResponse = memberService.createToken(memberId);

        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken(tokenResponse.getRefreshToken());

        //when
        AccessTokenResponse accessTokenResponse = memberService.refreshToken(refreshRequest);

        //then
        String accessToken = accessTokenResponse.getAccessToken();
        Long id = JWT.require(Algorithm.HMAC256(JwtProperties.SECRET_KEY))
                .build()
                .verify(accessToken)
                .getClaim("id")
                .asLong();

        Assertions
                .assertThat(id)
                .isEqualTo(memberId);
    }

    @Test
    @DisplayName("만료된 리프레쉬 토큰으로 갱신 시도")
    void expiredRefreshToken() {

        //given
        String refreshToken = JWT.create()
                .withExpiresAt(new Date(System.currentTimeMillis() - 1))
                .sign(Algorithm.HMAC256(JwtProperties.SECRET_KEY));

        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken(refreshToken);

        //when & then
        Assertions
                .assertThatThrownBy(() -> memberService.refreshToken(refreshRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage(ErrorCode.JWT_EXPIRED_TOKEN.getMessage());

    }

    @Test
    @DisplayName("로그아웃")
    void logout() throws AuthenticationException {

        //given
        Long memberId = 1L;
        TokenResponse tokenResponse = memberService.createToken(memberId);
        String refreshToken = tokenResponse.getRefreshToken();

        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setRefreshToken(refreshToken);

        //when
        memberService.logout(logoutRequest);

        //then
        Assertions
                .assertThat(redisTemplate.hasKey(refreshToken))
                .isFalse();
    }

    @Test
    @DisplayName("중복된 로그아웃 시도")
    void duplicateLogout() throws AuthenticationException {

        //given
        Long memberId = 1L;
        TokenResponse tokenResponse = memberService.createToken(memberId);
        String refreshToken = tokenResponse.getRefreshToken();

        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setRefreshToken(refreshToken);

        //when & then
        memberService.logout(logoutRequest);
        Assertions
                .assertThatThrownBy(() -> memberService.logout(logoutRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage(ErrorCode.JWT_LOGOUT_TOKEN.getMessage());
    }

}
