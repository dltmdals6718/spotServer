package com.example.spotserver.Integration;

import com.example.spotserver.domain.ImageStore;
import com.example.spotserver.domain.MailCertification;
import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.MemberImage;
import com.example.spotserver.dto.request.MemberUpdateRequest;
import com.example.spotserver.dto.request.SignInMember;
import com.example.spotserver.dto.request.SignUpMember;
import com.example.spotserver.exception.*;
import com.example.spotserver.repository.MailCertificationRepository;
import com.example.spotserver.repository.MemberRepository;
import com.example.spotserver.service.MailSerivce;
import com.example.spotserver.service.MemberService;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

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
    private MailCertificationRepository mailCertificationRepository;

    @Autowired
    private ImageStore imageStore;

    @Autowired
    private EntityManager em;

    Member member;
    String loginId = "testId";
    String loginPwd = "testPwd";
    String mail = "smsmsmtp@gmail.com";

    @BeforeEach
    void init() throws IOException {
        member = new Member();
        member.setName("테스트 닉네임");
        member.setLoginId(loginId);
        member.setLoginPwd(new BCryptPasswordEncoder().encode(loginPwd));
        memberRepository.save(member);

        MockMultipartFile profile = new MockMultipartFile("profile", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "".getBytes());
        MemberImage memberImage = imageStore.storeMemberImage(profile);
        memberImage.setMember(member);
        member.setMemberImg(memberImage);
    }

    @AfterEach
    void clearFile() {

        em.clear();

        MemberImage memberImg = member.getMemberImg();
        imageStore.deleteMemberImage(memberImg);
    }

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

        imageStore.deleteMemberImage(member.getMemberImg());
    }

    @Test
    @DisplayName("중복된 닉네임으로 가입 시도")
    void duplicateNameSignup() throws FileException, DuplicateException, IOException, MailException {

        //given
        Integer code = 12345;
        MailCertification mailCertification = new MailCertification();
        mailCertification.setMail(mail);
        mailCertification.setCode(code);
        mailCertificationRepository.save(mailCertification);

        SignUpMember signUpMember = new SignUpMember();
        signUpMember.setLoginId("중복 아닌 아이디");
        signUpMember.setLoginPwd("비밀번호");
        signUpMember.setMail(mail);
        signUpMember.setName(member.getName());
        signUpMember.setCode(code);


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
        Integer code = 12345;
        MailCertification mailCertification = new MailCertification();
        mailCertification.setMail(mail);
        mailCertification.setCode(code);
        mailCertificationRepository.save(mailCertification);

        SignUpMember signUpMember = new SignUpMember();
        signUpMember.setLoginId(member.getLoginId());
        signUpMember.setLoginPwd("비밀번호");
        signUpMember.setMail(mail);
        signUpMember.setName("중복 아닌 닉네임");
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

        Member loginMember = memberService.login(loginId, loginPwd, request.getRemoteAddr());
        Assertions
                .assertThat(member.getId())
                .isEqualTo(loginMember.getId());
    }

    @Test
    @DisplayName("로그인 실패")
    void failSignin() throws LoginFailException {

        MockHttpServletRequest request = new MockHttpServletRequest();

        Assertions
                .assertThatThrownBy(() -> memberService.login(loginId, loginPwd + "haha", request.getRemoteAddr()))
                .isInstanceOf(LoginFailException.class)
                .hasMessage(ErrorCode.FAIL_LOGIN.getMessage());
    }

    @Test
    @DisplayName("회원 정보 및 프로필 사진 변경")
    void updateMember() throws DuplicateException, IOException {

        //given
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest();
        memberUpdateRequest.setName("updateName");
        String uploadFileName = "updateImg.jpg";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("memberImg", uploadFileName, MediaType.IMAGE_JPEG_VALUE, "img".getBytes());

        //when
        memberService.updateMember(memberUpdateRequest, mockMultipartFile, member.getId());

        //then
        Member updateMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new NoSuchElementException());

        Assertions
                .assertThat(updateMember.getName())
                .isEqualTo(memberUpdateRequest.getName());

        Assertions
                .assertThat(member.getMemberImg().getUploadFileName())
                .isEqualTo(uploadFileName);
    }

    @Test
    @DisplayName("이메일 인증 번호 전송")
    void mailCertification() throws MessagingException, MailException {
        mailSerivce.sendMailCertification(mail);
        Assertions
                .assertThat(mailCertificationRepository.findByMail(mail))
                .isPresent();
    }

    @Test
    @DisplayName("5분내 이메일 인증 번호 재요청 에러")
    void failMailRequestInTime() throws MessagingException, MailException {

        //given
        mailSerivce.sendMailCertification(mail);

        //when & then
        Assertions
                .assertThatThrownBy(() -> mailSerivce.sendMailCertification(mail))
                .hasMessage(ErrorCode.FAIL_MAIL_CERTIFICATION_REQUEST.getMessage());
    }

    @Test
    @DisplayName("이메일 인증 미요청")
    void notMailCertificationRequest() throws FileException, DuplicateException, IOException, MailException {

        //given
        SignUpMember signUpMember = new SignUpMember();
        signUpMember.setLoginId("중복 아닌 아이디");
        signUpMember.setMail(mail);
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
        mailCertification.setMail(mail);
        mailCertification.setCode(code);
        mailCertificationRepository.save(mailCertification);

        SignUpMember signUpMember = new SignUpMember();
        signUpMember.setLoginId("중복 아닌 아이디");
        signUpMember.setLoginPwd("비밀번호");
        signUpMember.setMail(mail);
        signUpMember.setName("중복 아닌 닉네임");
        signUpMember.setCode(code+1);

        //when & then
        Assertions
                .assertThatThrownBy(() -> memberService.addMember(signUpMember, null))
                .isInstanceOf(MailException.class)
                .hasMessage(ErrorCode.FAIL_MAIL_CERTIFICATION.getMessage());

    }

    @Test
    @DisplayName("이메일 인증 제한 시간 초과")
    void failMailCertificationInTime() throws FileException, DuplicateException, IOException, MailException {

        //given
        Integer code = 12345;
        MailCertification mailCertification = new MailCertification();
        mailCertification.setMail(mail);
        mailCertification.setCode(code);
        mailCertificationRepository.save(mailCertification);

        MailCertification findMailCertification = mailCertificationRepository.findByMail(mail)
                .orElseThrow(() -> new NoSuchElementException());
        findMailCertification.setRegDate(LocalDateTime.now().minusDays(1));

        SignUpMember signUpMember = new SignUpMember();
        signUpMember.setLoginId("중복 아닌 아이디");
        signUpMember.setLoginPwd("비밀번호");
        signUpMember.setMail(mail);
        signUpMember.setName("중복 아닌 닉네임");
        signUpMember.setCode(code);

        //when & then
        Assertions
                .assertThatThrownBy(() -> memberService.addMember(signUpMember, null))
                .isInstanceOf(MailException.class)
                .hasMessage(ErrorCode.FAIL_MAIL_TIMEOUT.getMessage());

    }

}
