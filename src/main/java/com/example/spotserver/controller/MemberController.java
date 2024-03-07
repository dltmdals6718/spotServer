package com.example.spotserver.controller;


import com.example.spotserver.config.jwt.JwtProperties;
import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.MemberUpdateRequest;
import com.example.spotserver.dto.request.SignInMember;
import com.example.spotserver.dto.request.SignUpMember;
import com.example.spotserver.dto.response.MemberResponse;
import com.example.spotserver.exception.*;
import com.example.spotserver.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/members")
public class MemberController {

    private MemberService memberService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    @Autowired
    public MemberController(MemberService memberService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.memberService = memberService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @PostMapping("/signup")
    public ResponseEntity<MemberResponse> signupMember(@Valid @RequestPart SignUpMember signUpMember,
                                                       @RequestPart(required = false) MultipartFile memberImg) throws DuplicateException, IOException, MailException, FileException {

        MemberResponse memberResponse = memberService.addMember(signUpMember, memberImg);


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(memberResponse);
    }

    @PostMapping("/signin")
    public ResponseEntity<Map> signinMember(@Valid @RequestBody SignInMember signInMember) throws LoginFailException {

        System.out.println("MemberController.signinMember");

        String loginId = signInMember.getLoginId();
        String loginPwd = signInMember.getLoginPwd();
        Member member = memberService.login(loginId, loginPwd);

        Map<String, Object> tokenInfo = new HashMap<>();
        String token = memberService.createToken(member.getId());
        tokenInfo.put("token", token);
        tokenInfo.put("expire_in", JwtProperties.EXPIRE_TIME / 1000);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(tokenInfo);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable Long memberId) {
        MemberResponse memberResponse = memberService.getMemberInfo(memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(memberResponse);
    }

    @GetMapping
    public ResponseEntity<MemberResponse> getMyInfo(@AuthenticationPrincipal(expression = "member") Member member) {
        MemberResponse memberResponse = memberService.getMemberInfo(member.getId());

        return ResponseEntity
                .ok()
                .body(memberResponse);
    }

    @GetMapping("/{memberId}/images/{storeFileName}")
    public ResponseEntity<Resource> getMemberImg(@PathVariable Long memberId, @PathVariable String storeFileName) throws PermissionException, IOException {
        Resource memberImage = memberService.getMemberImage(memberId, storeFileName);

        String fileName = memberImage.getFilename();
        String extension = fileName.substring(fileName.indexOf('.') + 1).toLowerCase();

        String contentType;
        if (extension.equals("jpeg") || extension.equals("jpg"))
            contentType = MediaType.IMAGE_JPEG_VALUE;
        else if (extension.equals("png"))
            contentType = MediaType.IMAGE_PNG_VALUE;
        else
            contentType = MediaType.IMAGE_JPEG_VALUE;

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(memberImage);
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<MemberResponse> updateMember(@PathVariable Long memberId,
                                                       @Valid @RequestPart(required = false) MemberUpdateRequest memberUpdateRequest,
                                                       @RequestPart(required = false) MultipartFile memberImg,
                                                       @AuthenticationPrincipal(expression = "member") Member member) throws PermissionException, DuplicateException, IOException {

        if (!member.getId().equals(memberId))
            throw new PermissionException(ErrorCode.FORBIDDEN_CLIENT);

        MemberResponse memberResponse = memberService.updateMember(memberUpdateRequest, memberImg, member);

        return ResponseEntity
                .ok(memberResponse);
    }

    @GetMapping("/kakao")
    public ResponseEntity<Map> kakaoLogin(@RequestParam(name = "code") String code) {
        Member member = memberService.kakaoLogin(code);

        Map<String, Object> tokenInfo = new HashMap<>();
        String token = memberService.createToken(member.getId());
        tokenInfo.put("token", token);
        tokenInfo.put("expire_in", JwtProperties.EXPIRE_TIME / 1000);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(tokenInfo);
    }


    @ExceptionHandler(value = LoginFailException.class)
    public ResponseEntity<ErrorResponse> loginFailException(LoginFailException e) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse errorResponse = new ErrorResponse(errorCode);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

}
