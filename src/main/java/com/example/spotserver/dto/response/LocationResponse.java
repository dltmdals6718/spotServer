package com.example.spotserver.dto.response;

import com.example.spotserver.domain.Location;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class LocationResponse {

    private Long locationId;
    private Double latitude;
    private Double longitude;
    private String title;
    private String address;
    private String description;
    private LocalDateTime regDate;
    private Long likeCnt;


    public static LocationResponse toDto(Location location) {

        LocationResponse locationResponse = new LocationResponse();

        locationResponse.setLocationId(location.getId());
        locationResponse.setLatitude(location.getLatitude());
        locationResponse.setLongitude(location.getLongitude());
        locationResponse.setTitle(location.getTitle());
        locationResponse.setAddress(location.getAddress());
        locationResponse.setDescription(location.getDescription());

        return locationResponse;
    }

    @QueryProjection
    public LocationResponse(Long locationId, Double latitude, Double longitude, String title, String address, String description, LocalDateTime regDate, Long likeCnt) {
        this.locationId = locationId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.address = address;
        this.description = description;
        this.regDate = regDate;
        this.likeCnt = likeCnt;
    }
}
