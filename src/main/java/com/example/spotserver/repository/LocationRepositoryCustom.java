package com.example.spotserver.repository;


import com.example.spotserver.dto.request.LocationConditionRequest;
import com.example.spotserver.dto.response.LocationResponse;
import com.example.spotserver.dto.response.PosterResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LocationRepositoryCustom {

    List<LocationResponse> getBestLocations();
    Page<LocationResponse> searchLocations(Double latitude, Double longitude, LocationConditionRequest conditionRequest);
    Optional<LocationResponse> getLocationById(Long locationId);
    Page<LocationResponse> getLikeLocations(Long memberId, Pageable pageable);
}
