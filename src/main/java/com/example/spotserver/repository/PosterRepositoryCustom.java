package com.example.spotserver.repository;


import com.example.spotserver.dto.response.PosterResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PosterRepositoryCustom {

    Page<PosterResponse> searchPostersByRecent(Long locationId, Pageable pageable);
    Page<PosterResponse> searchPostersByLike(Long locationId, Pageable pageable);
}
