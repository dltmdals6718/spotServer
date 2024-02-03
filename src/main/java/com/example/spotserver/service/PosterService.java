package com.example.spotserver.service;

import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.PosterRequest;
import com.example.spotserver.dto.response.PageResponse;
import com.example.spotserver.dto.response.PosterResponse;
import com.example.spotserver.repository.LocationRepository;
import com.example.spotserver.repository.PosterImageRepository;
import com.example.spotserver.repository.PosterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PosterService {

    private PosterRepository posterRepository;
    private LocationRepository locationRepository;
    private PosterImageRepository posterImageRepository;
    private ImageStore imageStore;


    @Autowired
    public PosterService(PosterRepository posterRepository, LocationRepository locationRepository, PosterImageRepository posterImageRepository, ImageStore imageStore) {
        this.posterRepository = posterRepository;
        this.locationRepository = locationRepository;
        this.posterImageRepository = posterImageRepository;
        this.imageStore = imageStore;
    }

    public PosterResponse addPoster(PosterRequest posterRequest, List<MultipartFile> files, Long locationId, Member member) throws IOException {
        Poster poster = PosterRequest.toEntity(posterRequest);
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchElementException());

        poster.setWriter(member);
        poster.setLocation(location);

        posterRepository.save(poster);

        if (files != null) {
            List<PosterImage> imgFiles = imageStore.storePosterImages(files);
            for (PosterImage imgFile : imgFiles) {
                imgFile.setPoster(poster);
            }
            posterImageRepository.saveAll(imgFiles);
        }

        PosterResponse posterResponse = PosterResponse.toDto(poster);
        return posterResponse;
    }

    public PageResponse<List<PosterResponse>> getLocationPosters(Long locationId, Pageable pageable) {

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchElementException());

        Page<Poster> page = posterRepository.findByLocation(location, pageable);
        Page<PosterResponse> dtoPage = page.map((poster) -> PosterResponse.toDto(poster));

        PageResponse<List<PosterResponse>> pageResponse = new PageResponse<>(dtoPage);
        return pageResponse;
    }

    public PosterResponse getPoster(Long posterId) {
        Poster poster = posterRepository.findById(posterId)
                .orElseThrow(() -> new NoSuchElementException());
        PosterResponse posterResponse = PosterResponse.toDto(poster);
        return posterResponse;
    }
}
