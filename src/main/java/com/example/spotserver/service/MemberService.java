package com.example.spotserver.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.spotserver.config.jwt.JwtProperties;
import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.MemberType;
import com.example.spotserver.domain.Role;
import com.example.spotserver.dto.request.SignUpMember;
import com.example.spotserver.exception.DuplicateException;
import com.example.spotserver.exception.ErrorCode;
import com.example.spotserver.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class MemberService {

    private MemberRepository memberRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.memberRepository = memberRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public Member addMember(SignUpMember signUpMember) throws DuplicateException {


        String loginId = signUpMember.getLoginId();
        String name = signUpMember.getName();

        if (existLoginId(loginId)) {
            throw new DuplicateException(ErrorCode.DUPLICATE_LOGINID);
        }

        if (existName(name)) {
            throw new DuplicateException(ErrorCode.DUPLICATE_NAME);
        }


        Member member = signUpMember.toEntity(signUpMember);
        member.setRole(Role.USER);
        member.setLoginPwd(bCryptPasswordEncoder.encode(member.getLoginPwd()));
        member.setType(MemberType.NORMAL);

        Member resultMember = memberRepository.save(member);
        return resultMember;
    }

    public String createToken(Long memberId) {
        String jwtToken = JWT.create()
                .withSubject("톡톡토큰")
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRE_TIME))
                .withClaim("id", memberId)
                .sign(Algorithm.HMAC256(JwtProperties.SECRET_KEY));
        return jwtToken;
    }

    public Member getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException());
        return member;
    }

    public void deleteMemberById(Long memberId) {
        memberRepository.deleteById(memberId);
    }

    public boolean existLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    public boolean existName(String name) {
        return memberRepository.existsByName(name);
    }

    public Member findByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId);
    }

    public boolean existKakaoMember(Long snsId) {
        return memberRepository.existsByTypeIsAndSnsId(MemberType.KAKAO, snsId);
    }
}
