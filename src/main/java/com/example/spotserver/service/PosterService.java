package com.example.spotserver.service;

import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.PosterRequest;
import com.example.spotserver.dto.response.PageResponse;
import com.example.spotserver.dto.response.PosterResponse;
import com.example.spotserver.exception.ErrorCode;
import com.example.spotserver.exception.PermissionException;
import com.example.spotserver.repository.LocationRepository;
import com.example.spotserver.repository.PosterImageRepository;
import com.example.spotserver.repository.PosterRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
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

    public PosterResponse addPoster(PosterRequest posterRequest,
                                    List<MultipartFile> files,
                                    Long locationId,
                                    Member member) throws IOException {
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
        Page<PosterResponse> dtoPage = page
                .map((poster) -> PosterResponse.toDto(poster));

        PageResponse<List<PosterResponse>> pageResponse = new PageResponse<>(dtoPage);
        return pageResponse;
    }

    public PosterResponse getPoster(Long posterId) {
        Poster poster = posterRepository.findById(posterId)
                .orElseThrow(() -> new NoSuchElementException());
        PosterResponse posterResponse = PosterResponse.toDto(poster);
        return posterResponse;
    }

    public PosterResponse updatePoster(Long posterId,
                                       PosterRequest posterRequest,
                                       List<MultipartFile> addFiles,
                                       List<Long> deleteFilesId,
                                       Member member) throws IOException, PermissionException {

        Poster poster = posterRepository.findById(posterId)
                .orElseThrow(() -> new NoSuchElementException());

        if (!poster.getWriter().getId().equals(member.getId())) {
            throw new PermissionException(ErrorCode.FORBIDDEN_CLIENT);
        }

        if (deleteFilesId != null) {

            for (Long fileId : deleteFilesId) {

                PosterImage posterImage = posterImageRepository.findById(fileId)
                        .orElseThrow(() -> new NoSuchElementException());

                if (poster.getId().equals(posterImage.getPoster().getId())) {
                    String fullPath = imageStore.getPosterImgFullPath(posterImage.getStoreFileName());
                    File file = new File(fullPath);
                    if (file.exists())
                        file.delete();
                    posterImageRepository.deleteById(fileId);
                }

            }
        }

        poster.updatePoster(posterRequest);

        if (addFiles != null) {
            List<PosterImage> imgFiles = imageStore.storePosterImages(addFiles);
            for (PosterImage imgFile : imgFiles) {
                imgFile.setPoster(poster);
            }
            posterImageRepository.saveAll(imgFiles);
        }


        PosterResponse posterResponse = PosterResponse.toDto(poster);
        return posterResponse;
    }
}
