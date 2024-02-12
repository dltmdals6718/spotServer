package com.example.spotserver.controller;

import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.LocationRequest;
import com.example.spotserver.dto.response.LocationResponse;
import com.example.spotserver.exception.DuplicateException;
import com.example.spotserver.service.ImageFileService;
import com.example.spotserver.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private LocationService locationService;
    private ImageFileService imageFileService;
    private ImageStore imageStore;

    public LocationController(LocationService locationService, ImageFileService imageFileService, ImageStore imageStore) {
        this.locationService = locationService;
        this.imageFileService = imageFileService;
        this.imageStore = imageStore;
    }

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getLocations(@RequestParam("latitude") Double latitude,
                                                               @RequestParam("longitude") Double longitude) {
        List<Location> locations = locationService.getLocations(latitude, longitude);

        List<LocationResponse> locationResponseList = new ArrayList<>();
        for (Location location : locations) {
            locationResponseList.add(LocationResponse.toDto(location));
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(locationResponseList);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LocationResponse> addLocation(@Valid @RequestPart LocationRequest locationRequest,
                                                        @RequestPart(required = false) List<MultipartFile> files) throws IOException {

        Location location = LocationRequest.toEntity(locationRequest);

        locationService.addLocation(location);

        if (files != null) {
            List<LocationImage> imgFiles = imageStore.storeLocationImages(files);

            for (LocationImage imgFile : imgFiles) {
                imgFile.setLocation(location);
            }

            imageFileService.saveLocationImageList(imgFiles);
        }

        LocationResponse locationResponse = LocationResponse.toDto(location);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(locationResponse);
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<LocationResponse> getLocation(@PathVariable Long locationId) {

        Location location = locationService.getLocation(locationId);

        LocationResponse locationResponse = LocationResponse.toDto(location);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(locationResponse);
    }

    @GetMapping("/best")
    public ResponseEntity getBestLocation() {

        List<LocationResponse> bestLocations = locationService.getBestLocations();
        
        return ResponseEntity
                .ok(bestLocations);
    }

    @GetMapping("/{locationId}/likes")
    public ResponseEntity getLikes(@PathVariable Long locationId) {

        Map response = locationService.getLikes(locationId);

        return ResponseEntity
                .ok(response);
    }

    @PostMapping("/{locationId}/likes")
    public ResponseEntity addLike(@PathVariable Long locationId,
                                  @AuthenticationPrincipal(expression = "member") Member member) throws DuplicateException {

        locationService.addLike(locationId, member);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @DeleteMapping("/{locationId}/likes")
    public ResponseEntity deleteLike(@PathVariable Long locationId,
                                     @AuthenticationPrincipal(expression = "member") Member member) {

        locationService.deleteLike(locationId, member);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();

    }


}
