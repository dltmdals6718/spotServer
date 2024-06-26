package com.example.spotserver.controller;


import com.example.spotserver.config.jwt.JwtProperties;
import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.*;
import com.example.spotserver.dto.response.*;
import com.example.spotserver.exception.*;
import com.example.spotserver.service.LocationService;
import com.example.spotserver.service.MemberService;
import com.example.spotserver.service.PosterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/members")
@Slf4j
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
    public ResponseEntity<TokenResponse> signinMember(@Valid @RequestBody SignInMember signInMember,
                                            HttpServletRequest request) throws LoginFailException {

        String loginId = signInMember.getLoginId();
        String loginPwd = signInMember.getLoginPwd();
        Member member = memberService.login(loginId, loginPwd, request.getRemoteAddr());

        TokenResponse tokenResponse = memberService.createToken(member.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(tokenResponse);
    }

    @PostMapping(value = "/refresh")
    public ResponseEntity<AccessTokenResponse> refreshToken(@RequestBody RefreshRequest refreshRequest) throws AuthenticationException {

        AccessTokenResponse accessTokenResponse = memberService.refreshToken(refreshRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accessTokenResponse);
    }

    @PostMapping(value = "/logout")
    public ResponseEntity logout(@RequestBody LogoutRequest logoutRequest) throws AuthenticationException {

        memberService.logout(logoutRequest);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
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

    @PutMapping(value = "/{memberId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MemberResponse> updateMember(@PathVariable Long memberId,
                                                       @Valid @RequestPart(required = false) MemberUpdateRequest memberUpdateRequest,
                                                       @RequestPart(required = false) MultipartFile memberImg,
                                                       @AuthenticationPrincipal(expression = "member") Member member) throws PermissionException, DuplicateException, IOException, FileException {

        if (!member.getId().equals(memberId))
            throw new PermissionException(ErrorCode.FORBIDDEN_CLIENT);

        MemberResponse memberResponse = memberService.updateMember(memberUpdateRequest, memberImg, member.getId());

        return ResponseEntity
                .ok(memberResponse);
    }

    @DeleteMapping(value = "/{memberId}")
    public ResponseEntity deleteMember(@PathVariable Long memberId,
                                       @AuthenticationPrincipal(expression = "member") Member member) throws PermissionException {

        if(!memberId.equals(member.getId())) {
            throw new PermissionException(ErrorCode.FORBIDDEN_CLIENT);
        }

        memberService.deleteMember(memberId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PostMapping(value = "/signin-kakao", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenResponse> kakaoLogin(@RequestParam(name = "kakaoToken") String kakaoToken) {
        Member member = memberService.loginWithKakaoAccessToken(kakaoToken);

        TokenResponse tokenResponse = memberService.createToken(member.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(tokenResponse);
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

    @GetMapping("/{memberId}/posters")
    public ResponseEntity<PageResponse<PosterResponse>> writePosters(@PathVariable Long memberId,
                                                                     @RequestParam(defaultValue = "1") Integer page)  {

        PageResponse<PosterResponse> writePosters = posterService.getWritePosters(page, memberId);
        return ResponseEntity
                .ok(writePosters);
    }

    @GetMapping("/{memberId}/comments/posters")
    public ResponseEntity<PageResponse<PosterResponse>> getPostersByWriteComments(@PathVariable Long memberId,
                                                                                  @RequestParam(defaultValue = "1") Integer page) {

        PageResponse<PosterResponse> postersByWriteComments = posterService.getPostersByWriteComments(page, memberId);
        return ResponseEntity
                .ok(postersByWriteComments);
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
