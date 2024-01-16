package com.example.spotserver.controller;

import com.example.spotserver.domain.*;
import com.example.spotserver.service.ImageFileService;
import com.example.spotserver.service.LocationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
    public ApiResponse<Location> getLocations(@RequestParam("latitude") Double latitude, @RequestParam("longitude") Double longitude) {
        List<Location> locations = locationService.getLocations(latitude, longitude);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(ApiResponse.SUCCESS_STATUS);
        apiResponse.setData(locations);
        apiResponse.setMessage("올바른 데이터");
        return apiResponse;
    }

    @PostMapping
    public ApiResponse<Location> addLocation(@RequestPart Location location, @RequestPart(required = false) List<MultipartFile> files) throws IOException {
        if (files != null) {
            List<LocationImage> imgFiles = imageStore.storeLocationImages(files);
            imageFileService.saveLocationImageList(imgFiles);
            location.setImages(imgFiles);
        }

        locationService.addLocation(location);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(ApiResponse.SUCCESS_STATUS);
        apiResponse.setData(location);
        apiResponse.setMessage("올바른 데이터");

        // 응답으로 받은 locationId는 null로 나오는데 어떻게 응답을 내줄까?

        return apiResponse;
    }

}