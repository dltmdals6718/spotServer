package com.example.spotserver.controller;

import com.example.spotserver.domain.LocationImage;
import com.example.spotserver.domain.PosterImage;
import com.example.spotserver.dto.response.LocationImageResponse;
import com.example.spotserver.dto.response.PosterImageResponse;
import com.example.spotserver.service.ImageFileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ImageFileController {

    private ImageFileService imageFileService;


    @Autowired
    public ImageFileController(ImageFileService imageFileService) {
        this.imageFileService = imageFileService;
    }


    @GetMapping("/posters/{posterId}/images")
    public ResponseEntity<List<PosterImageResponse>> getPosterImageFilesInfo(@PathVariable Long posterId) {

        List<PosterImage> posterImageList = imageFileService.getPosterImageList(posterId);

        List<PosterImageResponse> posterImageResponseList = new ArrayList<>();
        for (PosterImage posterImage : posterImageList) {
            posterImageResponseList.add(PosterImageResponse.toDto(posterImage));
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(posterImageResponseList);
    }

    @GetMapping("/posters/images/{posterImageId}")
    public void getPosterImagefile(@PathVariable Long posterImageId, HttpServletResponse response) throws IOException {
        String posterImageUrl = imageFileService.getPosterImageUrl(posterImageId);
        response.sendRedirect(posterImageUrl);
    }

    @GetMapping("/locations/{locationId}/images")
    public ResponseEntity<List<LocationImageResponse>> getLocationImageFilesInfo(@PathVariable Long locationId) {

        List<LocationImage> locationImageList = imageFileService.getLocationImageList(locationId);

        List<LocationImageResponse> locationImageResponseList = new ArrayList<>();
        for (LocationImage locationImage : locationImageList) {
            locationImageResponseList.add(LocationImageResponse.toDto(locationImage));
        }

        return ResponseEntity
                .ok()
                .body(locationImageResponseList);
    }

    @GetMapping("/locations/images/{locationImageId}")
    public void getLocationImagefile(@PathVariable Long locationImageId, HttpServletResponse response) throws IOException {
        String locationImageUrl = imageFileService.getLocationImageUrl(locationImageId);
        response.sendRedirect(locationImageUrl);
    }


}
