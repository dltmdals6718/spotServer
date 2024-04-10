package com.example.spotserver.repository;


import com.example.spotserver.dto.request.PosterConditionRequest;
import com.example.spotserver.dto.response.PosterResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface PosterRepositoryCustom {

    Page<PosterResponse> searchPosters(Long locationId, PosterConditionRequest conditionRequest);
    Page<PosterResponse> getLikePosters(Long memberId, Pageable pageable);
    Optional<PosterResponse> getPosterById(Long posterId);
    List<PosterResponse> getBestPosters();
    Page<PosterResponse> getWritePosters(Long memberId, Pageable pageable);
    Page<PosterResponse> getPostersByWriteComments(Long memberId, Pageable pageable);
}
