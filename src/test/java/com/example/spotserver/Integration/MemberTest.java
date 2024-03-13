package com.example.spotserver.Integration;

import com.example.spotserver.domain.ImageStore;
import com.example.spotserver.domain.MailCertification;
import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.MemberImage;
import com.example.spotserver.dto.request.MemberUpdateRequest;
import com.example.spotserver.dto.request.SignInMember;
import com.example.spotserver.dto.request.SignUpMember;
import com.example.spotserver.exception.DuplicateException;
import com.example.spotserver.exception.FileException;
import com.example.spotserver.exception.LoginFailException;
import com.example.spotserver.exception.MailException;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.io.IOException;
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

    @BeforeEach
    void init() throws IOException {
        member = new Member();
        member.setName("테스트 닉네임");
        member.setLoginId(loginId);
        member.setLoginPwd(new BCryptPasswordEncoder().encode(loginPwd));

        MemberImage memberImage = new MemberImage();
        memberImage.setMember(member);
        String storeFileName = UUID.randomUUID() + ".jpg";
        memberImage.setStoreFileName(storeFileName);
        member.setMemberImg(memberImage);

        memberRepository.save(member);

        File file = new File(imageStore.getMemberImgFullPath(storeFileName));
        file.createNewFile();
    }

    @AfterEach
    void clearFile() {

        em.clear();

        MemberImage memberImg = member.getMemberImg();
        String storeFileName = memberImg.getStoreFileName();
        File file = new File(imageStore.getMemberImgFullPath(storeFileName));
        if(file.exists())
            file.delete();
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

        String storeFileName = member.getMemberImg().getStoreFileName();
        File file = new File(imageStore.getMemberImgFullPath(storeFileName));
        Assertions
                .assertThat(file.exists())
                .isTrue();

        if(file.exists())
            file.delete();
    }

    @Test
    @DisplayName("로그인")
    void signin() throws LoginFailException {
        Member loginMember = memberService.login(loginId, loginPwd);
        Assertions
                .assertThat(member.getId())
                .isEqualTo(loginMember.getId());
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
        String mail = "smsmsmtp@gmail.com";
        mailSerivce.sendMailCertification(mail);
        Assertions
                .assertThat(mailCertificationRepository.findByMail(mail))
                .isPresent();
    }

}
