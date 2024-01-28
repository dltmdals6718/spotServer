package com.example.spotserver.service;

import com.example.spotserver.domain.Location;
import com.example.spotserver.domain.Poster;
import com.example.spotserver.dto.response.PageResponse;
import com.example.spotserver.dto.response.PosterResponse;
import com.example.spotserver.repository.PosterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PosterService {

    private PosterRepository posterRepository;

    @Autowired
    public PosterService(PosterRepository posterRepository) {
        this.posterRepository = posterRepository;
    }

    public Long addPoster(Poster poster) {
        Poster saveInquiry = posterRepository.save(poster);
        return saveInquiry.getId();
    }

    public PageResponse<List<PosterResponse>> getLocationPosters(Location location, Pageable pageable) {
        Page<Poster> page = posterRepository.findByLocation(location, pageable);
        Page<PosterResponse> dtoPage = page.map((poster) -> PosterResponse.toDto(poster));

        PageResponse<List<PosterResponse>> pageResponse = new PageResponse<>(dtoPage);
        return pageResponse;
    }

    public Poster getPoster(Long posterId) {
        Poster poster = posterRepository.findById(posterId)
                .orElseThrow(() -> new NoSuchElementException());
        return poster;
    }
}
