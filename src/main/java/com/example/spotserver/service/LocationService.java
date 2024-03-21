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
import com.example.spotserver.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class LocationService {

    private LocationRepository locationRepository;
    private LocationLikeRepository locationLikeRepository;
    private MemberRepository memberRepository;
    private ImageStore imageStore;

    @Autowired
    public LocationService(LocationRepository locationRepository, LocationLikeRepository locationLikeRepository, MemberRepository memberRepository, ImageStore imageStore) {
        this.locationRepository = locationRepository;
        this.locationLikeRepository = locationLikeRepository;
        this.memberRepository = memberRepository;
        this.imageStore = imageStore;
    }

    public PageResponse<LocationResponse> searchLocations(Double latitude,
                                                                Double longitude,
                                                                LocationConditionRequest conditionRequest) {

        Page<LocationResponse> locationResponses = locationRepository.searchLocations(latitude, longitude, conditionRequest);
        PageResponse<LocationResponse> pageResponse = new PageResponse<>(locationResponses);
        return pageResponse;
    }

    public Location addLocation(Location location, List<MultipartFile> files) throws IOException {


        if (files != null) {
            List<LocationImage> imgFiles = imageStore.storeLocationImages(files);
            location.setLocationImages(imgFiles);

            for (LocationImage imgFile : imgFiles) {
                imgFile.setLocation(location);
            }
        }

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

    @Transactional
    public void deleteLocation(Long locationId) {

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchElementException());

        List<Poster> posters = location.getPosters();
        for (Poster poster : posters) {
            List<PosterImage> posterImages = poster.getPosterImages();
            for (PosterImage posterImage : posterImages) {
                imageStore.deletePosterImage(posterImage);
            }
        }

        List<LocationImage> locationImages = location.getLocationImages();
        for (LocationImage locationImage : locationImages) {
            imageStore.deleteLocationImage(locationImage);
        }

        locationRepository.deleteById(locationId);
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
                        Long memberId) throws DuplicateException {

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchElementException());

        Member member = memberRepository.findById(memberId)
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
                           Long memberId) {

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchElementException());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException());

        LocationLike locationLike = locationLikeRepository.findByLocationAndMember(location, member)
                .orElseThrow(() -> new NoSuchElementException());

        locationLikeRepository.delete(locationLike);

    }

    public PageResponse<LocationResponse> getLikeLocations(Integer page, Long memberId) {
        PageRequest pageRequest = PageRequest.of(page - 1, 5);
        Page<LocationResponse> likeLocations = locationRepository.getLikeLocations(memberId, pageRequest);
        PageResponse<LocationResponse> pageResponse = new PageResponse<>(likeLocations);
        return pageResponse;
    }
}
