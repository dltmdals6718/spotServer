package com.example.spotserver.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.spotserver.config.jwt.JwtProperties;
import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.MemberUpdateRequest;
import com.example.spotserver.dto.request.SignUpMember;
import com.example.spotserver.dto.response.MemberResponse;
import com.example.spotserver.exception.DuplicateException;
import com.example.spotserver.exception.ErrorCode;
import com.example.spotserver.exception.LoginFailException;
import com.example.spotserver.exception.PermissionException;
import com.example.spotserver.repository.MemberImageRepository;
import com.example.spotserver.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class MemberService {

    private MemberRepository memberRepository;
    private MemberImageRepository memberImageRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private ImageStore imageStore;

    @Autowired
    public MemberService(MemberRepository memberRepository, MemberImageRepository memberImageRepository, BCryptPasswordEncoder bCryptPasswordEncoder, ImageStore imageStore) {
        this.memberRepository = memberRepository;
        this.memberImageRepository = memberImageRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.imageStore = imageStore;
    }

    public MemberResponse addMember(SignUpMember signUpMember, MultipartFile memberImg) throws DuplicateException, IOException {


        String loginId = signUpMember.getLoginId();
        String name = signUpMember.getName();

        if (memberRepository.existsByLoginId(loginId)) {
            throw new DuplicateException(ErrorCode.DUPLICATE_LOGINID);
        }

        if (memberRepository.existsByName(name)) {
            throw new DuplicateException(ErrorCode.DUPLICATE_NAME);
        }


        Member member = signUpMember.toEntity(signUpMember);
        member.setRole(Role.USER);
        member.setLoginPwd(bCryptPasswordEncoder.encode(member.getLoginPwd()));
        member.setType(MemberType.NORMAL);

        if (memberImg != null) {
            MemberImage memberImage = imageStore.storeMemberImage(memberImg);
            memberImage.setMember(member);
            member.setMemberImg(memberImage);
        }

        Member resultMember = memberRepository.save(member);

        MemberResponse memberResponse = MemberResponse.toDto(resultMember);
        return memberResponse;
    }

    public String createToken(Long memberId) {
        String jwtToken = JWT.create()
                .withSubject("톡톡토큰")
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRE_TIME))
                .withClaim("id", memberId)
                .sign(Algorithm.HMAC256(JwtProperties.SECRET_KEY));
        return jwtToken;
    }

    public MemberResponse getMemberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException());

        MemberResponse memberResponse = MemberResponse.toDto(member);
        return memberResponse;
    }

    public Resource getMemberImage(Long memberId, String storeFileName) throws PermissionException, MalformedURLException {

        MemberImage memberImage = memberImageRepository.findByStoreFileName(storeFileName)
                .orElseThrow(() -> new NoSuchElementException());

        if (!memberId.equals(memberImage.getMember().getId()))
            throw new PermissionException(ErrorCode.FORBIDDEN_CLIENT);

        UrlResource resource = new UrlResource("file:" + imageStore.getMemberImgFullPath(storeFileName));
        if (!resource.exists())
            throw new NoSuchElementException();

        return resource;
    }

    @Transactional
    public MemberResponse updateMember(MemberUpdateRequest memberUpdateRequest, MultipartFile memberImg, Member member) throws DuplicateException, IOException {
        member = memberRepository.findById(member.getId())
                .orElseThrow(() -> new NoSuchElementException());

        if (memberUpdateRequest != null) {
            String name = memberUpdateRequest.getName();
            if (!name.equals(member.getName())) {
                if (memberRepository.existsByName(name))
                    throw new DuplicateException(ErrorCode.DUPLICATE_NAME);
                else {
                    member.setName(name);
                }
            }
        }

        if (memberImg != null) {
            MemberImage beforeImg = member.getMemberImg();
            if (beforeImg != null) {
                member.setMemberImg(null);
                memberImageRepository.deleteById(beforeImg.getId());
                String fullPath = imageStore.getMemberImgFullPath(beforeImg.getStoreFileName());
                File file = new File(fullPath);
                if (file.exists())
                    file.delete();
            }

            MemberImage memberImage = imageStore.storeMemberImage(memberImg);
            memberImage.setMember(member);
            member.setMemberImg(memberImage);
            memberImageRepository.save(memberImage);
        }

        return MemberResponse.toDto(member);
    }

    public void testDeleteMemberById(Long memberId) {

        Member member = memberRepository.findById(memberId).get();
        if (member.getMemberImg() != null) {
            String fullPath = imageStore.getMemberImgFullPath(member.getMemberImg().getStoreFileName());
            File file = new File(fullPath);
            if (file.exists())
                file.delete();
        }

        memberRepository.deleteById(memberId);
    }

    public Member login(String loginId, String loginPwd) throws LoginFailException {


        Member findMember = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new LoginFailException(ErrorCode.FAIL_LOGIN));

        if (!bCryptPasswordEncoder.matches(loginPwd, findMember.getLoginPwd()))
            throw new LoginFailException(ErrorCode.FAIL_LOGIN);

        return findMember;
    }

    public boolean existKakaoMember(Long snsId) {
        return memberRepository.existsByTypeIsAndSnsId(MemberType.KAKAO, snsId);
    }
}
