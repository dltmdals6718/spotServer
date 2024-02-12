package com.example.spotserver.dto.response;

import com.example.spotserver.domain.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationResponse {

    private Long locationId;
    private Double latitude;
    private Double longitude;
    private String title;
    private String address;
    private String description;
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
}
