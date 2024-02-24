package com.example.spotserver.service;

import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.ApproveRequest;
import com.example.spotserver.dto.request.LocationConditionRequest;
import com.example.spotserver.dto.response.ApproveResponse;
import com.example.spotserver.dto.response.LocationResponse;
import com.example.spotserver.dto.response.PageResponse;
import com.example.spotserver.dto.response.PosterResponse;
import com.example.spotserver.exception.DuplicateException;
import com.example.spotserver.exception.ErrorCode;
import com.example.spotserver.repository.LocationLikeRepository;
import com.example.spotserver.repository.LocationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class LocationService {

    private LocationRepository locationRepository;
    private LocationLikeRepository locationLikeRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository,
                           LocationLikeRepository locationLikeRepository) {
        this.locationRepository = locationRepository;
        this.locationLikeRepository = locationLikeRepository;
    }

    public PageResponse<List<LocationResponse>> searchLocations(Double latitude,
                                       Double longitude,
                                       LocationConditionRequest conditionRequest) {

        Page<LocationResponse> locationResponses = locationRepository.searchLocations(latitude, longitude, conditionRequest);
        PageResponse<List<LocationResponse>> pageResponse = new PageResponse<>(locationResponses);
        return pageResponse;
    }

    public Location addLocation(Location location) {
        Location saveLocation = locationRepository.save(location);
        return saveLocation;
    }

    @Transactional
    public ApproveResponse updateApprove(Long locationId, ApproveRequest approveRequest) {

        Boolean approve = approveRequest.getApprove();

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchElementException());

        location.setApprove(approve);

        ApproveResponse approveResponse = new ApproveResponse(locationId, approve);
        return approveResponse;
    }

    public LocationResponse getLocation(Long locationId) {
        LocationResponse locationResponse = locationRepository.getLocationById(locationId)
                .orElseThrow(() -> new NoSuchElementException());

        return locationResponse;
    }

    public List<LocationResponse> getBestLocations() {

        List<LocationResponse> bestLocations = locationRepository.getBestLocations();

        return bestLocations;
    }

    public Map getLikes(Long locationId) {

        Map<String, Object> response = new HashMap();

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchElementException());

        Long likeCnt = locationLikeRepository.countByLocation(location);
        response.put("likeCnt", likeCnt);

        return response;
    }

    public void addLike(Long locationId,
                        Member member) throws DuplicateException {

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchElementException());

        if(locationLikeRepository.existsLocationLikeByLocationAndMember(location, member)) {
            throw new DuplicateException(ErrorCode.DUPLICATE_LIKE);
        }

        LocationLike locationLike = new LocationLike();
        locationLike.setLocation(location);
        locationLike.setMember(member);
        locationLikeRepository.save(locationLike);

    }

    public void deleteLike(Long locationId,
                           Member member) {

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchElementException());

        LocationLike locationLike = locationLikeRepository.findByLocationAndMember(location, member)
                .orElseThrow(() -> new NoSuchElementException());

        locationLikeRepository.delete(locationLike);

    }
}
