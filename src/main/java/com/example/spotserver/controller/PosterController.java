package com.example.spotserver.controller;

import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.PosterPageRequest;
import com.example.spotserver.dto.request.PosterRequest;
import com.example.spotserver.dto.response.PageResponse;
import com.example.spotserver.dto.response.PosterResponse;
import com.example.spotserver.service.ImageFileService;
import com.example.spotserver.service.LocationService;
import com.example.spotserver.service.PosterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class PosterController {

    private PosterService posterService;


    @Autowired
    public PosterController(PosterService posterService) {
        this.posterService = posterService;
    }

    @PostMapping(value = "/locations/{locationId}/posters", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PosterResponse> addPoster(@Valid @RequestPart PosterRequest posterRequest,
                                                    @RequestPart(required = false) List<MultipartFile> files,
                                                    @PathVariable Long locationId,
                                                    @AuthenticationPrincipal(expression = "member") Member member) throws IOException {

        PosterResponse posterResponse = posterService.addPoster(posterRequest, files, locationId, member);


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(posterResponse);
    }

    @GetMapping("/locations/{locationId}/posters")
    public ResponseEntity<PageResponse<List<PosterResponse>>> getLocationPosters(@PathVariable Long locationId,
                                                                                 @Valid @ModelAttribute PosterPageRequest posterPageRequest) {

        //요청 파라미터로 들어온 이름, 나이를 객체를 생성하여 값을 넣어주는 과정을 거친다.
        //위의 과정을 스프링 @ModelAttribute로 자동화 할 수 있다.
        PageRequest pageRequest = posterPageRequest.makePageRequest();

        PageResponse<List<PosterResponse>> posters = posterService.getLocationPosters(locationId, pageRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(posters);
    }

    @GetMapping("/posters/{posterId}")
    public ResponseEntity<PosterResponse> getPoster(@PathVariable Long posterId) {
        PosterResponse posterResponse = posterService.getPoster(posterId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(posterResponse);
    }

}
