package com.example.spotserver.repository;


import com.example.spotserver.dto.response.LocationResponse;

import java.util.List;

public interface LocationRepositoryCustom {

    List<LocationResponse> getBestLocations();
}
