package com.example.spotserver.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.spotserver.config.jwt.JwtProperties;
import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.MemberUpdateRequest;
import com.example.spotserver.dto.request.SignUpMember;
import com.example.spotserver.dto.response.MemberResponse;
import com.example.spotserver.dto.response.TokenResponse;
import com.example.spotserver.exception.*;
import com.example.spotserver.repository.MailCertificationRepository;
import com.example.spotserver.repository.MemberImageRepository;
import com.example.spotserver.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class MemberService {

    private MemberRepository memberRepository;
    private MemberImageRepository memberImageRepository;
    private MailCertificationRepository mailCertificationRepository;
    private RedisTemplate<String, Long> redisTemplate;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private ImageStore imageStore;
    private Logger loginLogger = LoggerFactory.getLogger("login");

    @Autowired
    public MemberService(MemberRepository memberRepository, MemberImageRepository memberImageRepository, MailCertificationRepository mailCertificationRepository, RedisTemplate redisTemplate, BCryptPasswordEncoder bCryptPasswordEncoder, ImageStore imageStore) {
        this.memberRepository = memberRepository;
        this.memberImageRepository = memberImageRepository;
        this.mailCertificationRepository = mailCertificationRepository;
        this.redisTemplate = redisTemplate;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.imageStore = imageStore;
    }

    @Transactional(rollbackOn = {FileException.class})
    public MemberResponse addMember(SignUpMember signUpMember, MultipartFile memberImg) throws DuplicateException, IOException, MailException, FileException {


        String loginId = signUpMember.getLoginId();
        String name = signUpMember.getName();
        String mail = signUpMember.getMail();
        Integer code = signUpMember.getCode();

        if (memberRepository.existsByLoginId(loginId)) {
            throw new DuplicateException(ErrorCode.DUPLICATE_LOGINID);
        }

        if (memberRepository.existsByName(name)) {
            throw new DuplicateException(ErrorCode.DUPLICATE_NAME);
        }

        if (memberRepository.existsByMail(mail)) {
            throw new DuplicateException(ErrorCode.DUPLICATE_MAIL);
        }

        MailCertification mailCertification = mailCertificationRepository.findByMail(mail)
                .orElseThrow(() -> new MailException(ErrorCode.NOT_MAIL_CERTIFICATION));

        if (!mailCertification.getCode().equals(code)) {
            throw new MailException(ErrorCode.FAIL_MAIL_CERTIFICATION);
        } else {

            LocalDateTime regDate = mailCertification.getRegDate();
            LocalDateTime now = LocalDateTime.now();

            if(now.isAfter(regDate.plusMinutes(5))) {
                mailCertificationRepository.delete(mailCertification);
                throw new MailException(ErrorCode.FAIL_MAIL_TIMEOUT);
            }

            mailCertificationRepository.delete(mailCertification);
        }


        Member member = signUpMember.toEntity(signUpMember);
        member.setLoginPwd(bCryptPasswordEncoder.encode(member.getLoginPwd()));

        if (memberImg != null) {

            if(memberImg.getSize() > 1000000) {
                throw new FileException(ErrorCode.FAIL_FILE_SIZE);
            }

            String extension = ImageStore.getFileExtension(memberImg.getOriginalFilename());
            List<String> supportFile = new ArrayList<>(Arrays.asList("jpeg", "jpg", "png"));
            if(!supportFile.contains(extension))
                throw new FileException(ErrorCode.NOT_SUPPORT_FILE);

            MemberImage memberImage = imageStore.storeMemberImage(memberImg);
            memberImage.setMember(member);
            member.setMemberImg(memberImage);
        }

        Member resultMember = memberRepository.save(member);

        MemberResponse memberResponse = MemberResponse.toDto(resultMember, imageStore);
        return memberResponse;
    }

    @Transactional
    public void deleteMember(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException());

        // 프로필 이미지 삭제
        MemberImage memberImg = member.getMemberImg();
        imageStore.deleteMemberImage(memberImg);

        // 작성한 게시글 이미지 삭제
        List<Poster> posters = member.getPosters();
        for (Poster poster : posters) {
            for (PosterImage posterImage : poster.getPosterImages()) {
                imageStore.deletePosterImage(posterImage);
            }
        }

        memberRepository.deleteById(memberId);
    }

    public TokenResponse createToken(Long memberId) {

        TokenResponse tokenResponse = new TokenResponse();

        String accessToken = JWT.create()
                .withSubject("AccessToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.ACCESS_TOKEN_EXPIRE_TIME * 1000))
                .withClaim("id", memberId)
                .sign(Algorithm.HMAC256(JwtProperties.SECRET_KEY));

        String refreshToken = UUID.randomUUID().toString();

        tokenResponse.setToken(accessToken);
        tokenResponse.setExpire_in(JwtProperties.ACCESS_TOKEN_EXPIRE_TIME);

        tokenResponse.setRefreshToken(refreshToken);
        tokenResponse.setRefreshExpireIn(JwtProperties.REFRESH_TOKEN_EXPIRE_TIME);

        ValueOperations<String, Long> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(refreshToken, memberId);
        redisTemplate.expire(refreshToken, JwtProperties.REFRESH_TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);

        return tokenResponse;
    }

    public MemberResponse getMemberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException());

        MemberResponse memberResponse = MemberResponse.toDto(member, imageStore);
        return memberResponse;
    }

    @Transactional
    public MemberResponse updateMember(MemberUpdateRequest memberUpdateRequest, MultipartFile memberImg, Long memberId) throws DuplicateException, IOException, FileException {

        Member member = memberRepository.findById(memberId)
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
            String extension = ImageStore.getFileExtension(memberImg.getOriginalFilename());
            List<String> supportFile = new ArrayList<>(Arrays.asList("jpeg", "jpg", "png"));
            if(!supportFile.contains(extension))
                throw new FileException(ErrorCode.NOT_SUPPORT_FILE);

            MemberImage beforeImg = member.getMemberImg();
            if (beforeImg != null) {
                imageStore.deleteMemberImage(beforeImg);
                member.setMemberImg(null);
                memberImageRepository.deleteById(beforeImg.getId());
            }

            MemberImage memberImage = imageStore.storeMemberImage(memberImg);
            memberImage.setMember(member);
            member.setMemberImg(memberImage);
            memberImageRepository.save(memberImage);
        }

        return MemberResponse.toDto(member, imageStore);
    }


    public Member login(String loginId, String loginPwd, String ip) throws LoginFailException {


        Member findMember = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new LoginFailException(ErrorCode.FAIL_LOGIN));

        if (!bCryptPasswordEncoder.matches(loginPwd, findMember.getLoginPwd())) {
            loginLogger.info("[로그인 실패] ip : {}, id : {}, member.name : {}", ip, findMember.getLoginId(), findMember.getName());
            throw new LoginFailException(ErrorCode.FAIL_LOGIN);
        }

        loginLogger.info("[로그인 성공] ip : {}, id : {}, member.name : {}", ip, findMember.getLoginId(), findMember.getName());
        return findMember;
    }

    public Member loginWithKakaoAccessToken(String kakaoAccessToken) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        httpHeaders.add("Authorization", "Bearer " + kakaoAccessToken);

        HttpEntity httpEntity = new HttpEntity(httpHeaders);

        ResponseEntity<String> token_response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        JSONObject jsonObject = new JSONObject(token_response.getBody());
        JSONObject properties = jsonObject.getJSONObject("properties");
        long snsId = jsonObject.getLong("id");
        String name = properties.getString("nickname");

        Optional<Member> oldMember = memberRepository.findByTypeAndSnsId(MemberType.KAKAO, snsId);
        if(oldMember.isPresent()) {
            return oldMember.get();
        } else {
            Member newMember = new Member();
            newMember.setType(MemberType.KAKAO);
            newMember.setName(name);
            newMember.setSnsId(snsId);
            newMember.setRole(Role.USER);
            memberRepository.save(newMember);
            return newMember;
        }
    }
}
