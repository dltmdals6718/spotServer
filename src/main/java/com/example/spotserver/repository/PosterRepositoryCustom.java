package com.example.spotserver.repository;


import com.example.spotserver.dto.response.PosterResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface PosterRepositoryCustom {

    Page<PosterResponse> searchPostersByRecent(Long locationId, Pageable pageable);
    Page<PosterResponse> searchPostersByLike(Long locationId, Pageable pageable);
    Page<PosterResponse> getLikePosters(Long memberId, Pageable pageable);
    Optional<PosterResponse> getPosterById(Long posterId);
    List<PosterResponse> getBestPosters();
}
