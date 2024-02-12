package com.example.spotserver.repository;

import com.example.spotserver.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long>, LocationRepositoryCustom {
    List<Location> findByLatitudeBetweenAndLongitudeBetween(double latitudeMinValue, double latitudeMaxValue, double longitudeMinValue, double longitudeMaxValue);
}
