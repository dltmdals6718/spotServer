package com.example.spotserver.controller;

import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.ApproveRequest;
import com.example.spotserver.dto.request.LocationConditionRequest;
import com.example.spotserver.dto.request.LocationRequest;
import com.example.spotserver.dto.response.ApproveResponse;
import com.example.spotserver.dto.response.LocationResponse;
import com.example.spotserver.dto.response.PageResponse;
import com.example.spotserver.exception.DuplicateException;
import com.example.spotserver.exception.ErrorCode;
import com.example.spotserver.exception.PermissionException;
import com.example.spotserver.service.ImageFileService;
import com.example.spotserver.service.LocationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

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
    public ResponseEntity<PageResponse<List<LocationResponse>>> getLocations(@RequestParam("latitude") Double latitude,
                                                                             @RequestParam("longitude") Double longitude,
                                                                             @Valid @ModelAttribute LocationConditionRequest conditionRequest) throws PermissionException {

        Boolean approve = conditionRequest.getApprove();
        if (!approve) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            boolean containsAdmin = authorities.contains(new SimpleGrantedAuthority(Role.ADMIN.toString()));

            if(!containsAdmin) {
                throw new PermissionException(ErrorCode.FORBIDDEN_CLIENT);
            }
        }


        PageResponse<List<LocationResponse>> pageResponse = locationService.searchLocations(latitude, longitude, conditionRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(pageResponse);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map> addLocation(@Valid @RequestPart LocationRequest locationRequest,
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

        Map<String, Object> response = new HashMap<>();
        response.put("locationId", location.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{locationId}/approve")
    public ResponseEntity<ApproveResponse> updateLocationApprove(@PathVariable Long locationId, @RequestBody ApproveRequest approveRequest) {

        ApproveResponse approveResponse = locationService.updateApprove(locationId, approveRequest);

        return ResponseEntity
                .ok(approveResponse);
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<LocationResponse> getLocation(@PathVariable Long locationId) {

        LocationResponse locationResponse = locationService.getLocation(locationId);

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
