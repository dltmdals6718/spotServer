package com.example.spotserver.repository;

import com.example.spotserver.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationLikeRepository extends JpaRepository<LocationLike, Long> {

    boolean existsLocationLikeByLocationAndMember(Location location, Member member);
    Optional<LocationLike> findByLocationAndMember(Location location, Member member);
    Long countByLocation(Location location);

}
