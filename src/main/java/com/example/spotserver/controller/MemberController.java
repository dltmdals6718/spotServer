package com.example.spotserver.controller;


import com.example.spotserver.config.jwt.JwtProperties;
import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.MemberUpdateRequest;
import com.example.spotserver.dto.request.SignInMember;
import com.example.spotserver.dto.request.SignUpMember;
import com.example.spotserver.dto.response.LocationResponse;
import com.example.spotserver.dto.response.MemberResponse;
import com.example.spotserver.dto.response.PageResponse;
import com.example.spotserver.dto.response.PosterResponse;
import com.example.spotserver.exception.*;
import com.example.spotserver.service.LocationService;
import com.example.spotserver.service.MemberService;
import com.example.spotserver.service.PosterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/members")
public class MemberController {

    private MemberService memberService;
    private PosterService posterService;
    private LocationService locationService;


    @Autowired
    public MemberController(MemberService memberService, PosterService posterService, LocationService locationService) {
        this.memberService = memberService;
        this.posterService = posterService;
        this.locationService = locationService;
    }

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MemberResponse> signupMember(@Valid @RequestPart SignUpMember signUpMember,
                                                       @RequestPart(required = false) MultipartFile memberImg) throws DuplicateException, IOException, MailException, FileException {

        MemberResponse memberResponse = memberService.addMember(signUpMember, memberImg);


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(memberResponse);
    }

    @PostMapping(value = "/signin", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> signinMember(@Valid @RequestBody SignInMember signInMember) throws LoginFailException {

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

    @PutMapping(value = "/{memberId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MemberResponse> updateMember(@PathVariable Long memberId,
                                                       @Valid @RequestPart(required = false) MemberUpdateRequest memberUpdateRequest,
                                                       @RequestPart(required = false) MultipartFile memberImg,
                                                       @AuthenticationPrincipal(expression = "member") Member member) throws PermissionException, DuplicateException, IOException {

        if (!member.getId().equals(memberId))
            throw new PermissionException(ErrorCode.FORBIDDEN_CLIENT);

        MemberResponse memberResponse = memberService.updateMember(memberUpdateRequest, memberImg, member.getId());

        return ResponseEntity
                .ok(memberResponse);
    }

    @PostMapping(value = "/signin-kakao", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> kakaoLogin(@RequestParam(name = "kakaoToken") String kakaoToken) {
        Member member = memberService.loginWithKakaoAccessToken(kakaoToken);

        Map<String, Object> tokenInfo = new HashMap<>();
        String token = memberService.createToken(member.getId());
        tokenInfo.put("token", token);
        tokenInfo.put("expire_in", JwtProperties.EXPIRE_TIME / 1000);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(tokenInfo);
    }

    @GetMapping("/like-posters")
    public ResponseEntity<PageResponse<PosterResponse>> likePosters(@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : member") Member member,
                                                                    @RequestParam(defaultValue = "1") Integer page) throws AuthenticationException {

        if (member == null)
            throw new AuthenticationException(ErrorCode.UNAUTHORIZED_CLIENT);

        PageResponse<PosterResponse> likePosters = posterService.getLikePosters(page, member.getId());
        return ResponseEntity
                .ok(likePosters);
    }

    @GetMapping("/like-locations")
    public ResponseEntity<PageResponse<LocationResponse>> likeLocations(@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : member") Member member,
                                                                        @RequestParam(defaultValue = "1") Integer page) throws AuthenticationException {

        if (member == null)
            throw new AuthenticationException(ErrorCode.UNAUTHORIZED_CLIENT);

        PageResponse<LocationResponse> likeLocations = locationService.getLikeLocations(page, member.getId());
        return ResponseEntity
                .ok(likeLocations);
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
