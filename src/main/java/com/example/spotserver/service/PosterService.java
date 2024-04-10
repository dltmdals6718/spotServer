package com.example.spotserver.service;

import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.PosterConditionRequest;
import com.example.spotserver.dto.request.PosterRequest;
import com.example.spotserver.dto.response.PageResponse;
import com.example.spotserver.dto.response.PosterResponse;
import com.example.spotserver.exception.DuplicateException;
import com.example.spotserver.exception.ErrorCode;
import com.example.spotserver.exception.PermissionException;
import com.example.spotserver.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class PosterService {

    private PosterRepository posterRepository;
    private PosterLikeRepository posterLikeRepository;
    private LocationRepository locationRepository;
    private PosterImageRepository posterImageRepository;
    private MemberRepository memberRepository;
    private ImageStore imageStore;


    @Autowired
    public PosterService(PosterRepository posterRepository, PosterLikeRepository posterLikeRepository, LocationRepository locationRepository, PosterImageRepository posterImageRepository, ImageStore imageStore, MemberRepository memberRepository) {
        this.posterRepository = posterRepository;
        this.posterLikeRepository = posterLikeRepository;
        this.locationRepository = locationRepository;
        this.posterImageRepository = posterImageRepository;
        this.imageStore = imageStore;
        this.memberRepository = memberRepository;
    }

    public Long addPoster(Poster poster,
                          List<MultipartFile> files,
                          Long locationId,
                          Long memberId) throws IOException {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchElementException());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException());

        poster.setWriter(member);
        poster.setLocation(location);


        if (files != null) {
            List<PosterImage> imgFiles = imageStore.storePosterImages(files);
            poster.setPosterImages(imgFiles);
            for (PosterImage imgFile : imgFiles) {
                imgFile.setPoster(poster);
            }
        }

        Poster savePoster = posterRepository.save(poster);

        return savePoster.getId();
    }

    public PageResponse<PosterResponse> getLocationPosters(Long locationId, PosterConditionRequest conditionRequest) {

        Page<PosterResponse> posters = posterRepository.searchPosters(locationId, conditionRequest);
        PageResponse<PosterResponse> pageResponse = new PageResponse<>(posters);
        return pageResponse;
    }

    public PosterResponse getPoster(Long posterId) {
        PosterResponse posterResponse = posterRepository.getPosterById(posterId)
                .orElseThrow(() -> new NoSuchElementException());
        return posterResponse;
    }

    @Transactional
    public void updatePoster(Long posterId,
                             PosterRequest posterRequest,
                             List<MultipartFile> addFiles,
                             List<Long> deleteFilesId,
                             Long memberId) throws IOException, PermissionException {

        Poster poster = posterRepository.findById(posterId)
                .orElseThrow(() -> new NoSuchElementException());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException());

        if (!poster.getWriter().getId().equals(member.getId())) {
            throw new PermissionException(ErrorCode.FORBIDDEN_CLIENT);
        }

        if (deleteFilesId != null) {

            for (Long fileId : deleteFilesId) {

                PosterImage posterImage = posterImageRepository.findById(fileId)
                        .orElseThrow(() -> new NoSuchElementException());

                if (poster.getId().equals(posterImage.getPoster().getId())) {
                    imageStore.deletePosterImage(posterImage);
                    posterImageRepository.deleteById(fileId);
                    poster.getPosterImages().remove(posterImage);
                }
            }
        }

        poster.updatePoster(posterRequest);

        if (addFiles != null) {
            List<PosterImage> imgFiles = imageStore.storePosterImages(addFiles);

            if (poster.getPosterImages() == null)
                poster.setPosterImages(imgFiles);
            else
                poster.getPosterImages().addAll(imgFiles);

            for (PosterImage imgFile : imgFiles) {
                imgFile.setPoster(poster);
            }
        }

    }

    public void deletePoster(Long posterId, Long memberId) throws PermissionException {

        Poster poster = posterRepository.findById(posterId)
                .orElseThrow(() -> new NoSuchElementException());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException());

        if (!member.getId().equals(poster.getWriter().getId()))
            throw new PermissionException(ErrorCode.FORBIDDEN_CLIENT);

        List<PosterImage> posterImages = poster.getPosterImages();
        if (posterImages != null) {
            for (PosterImage posterImage : posterImages) {
                imageStore.deletePosterImage(posterImage);
            }

        }

        posterRepository.delete(poster);

    }

    public Map getLikes(Long posterId) {

        Map<String, Object> response = new HashMap();

        Poster poster = posterRepository.findById(posterId)
                .orElseThrow(() -> new NoSuchElementException());

        Long likeCnt = posterLikeRepository.countByPoster(poster);
        response.put("likeCnt", likeCnt);

        return response;
    }

    public void addLike(Long posterId, Long memberId) throws DuplicateException {

        Poster poster = posterRepository.findById(posterId)
                .orElseThrow(() -> new NoSuchElementException());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException());

        if (posterLikeRepository.existsPosterLikeByPosterAndMember(poster, member)) {
            throw new DuplicateException(ErrorCode.DUPLICATE_LIKE);
        }

        PosterLike posterLike = new PosterLike();
        posterLike.setPoster(poster);
        posterLike.setMember(member);
        posterLikeRepository.save(posterLike);

    }

    public void deleteLike(Long posterId, Long memberId) {

        Poster poster = posterRepository.findById(posterId)
                .orElseThrow(() -> new NoSuchElementException());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException());

        PosterLike posterLike = posterLikeRepository.findByPosterAndMember(poster, member)
                .orElseThrow(() -> new NoSuchElementException());

        posterLikeRepository.delete(posterLike);

    }

    public PageResponse<PosterResponse> getLikePosters(Integer page, Long memberId) {
        PageRequest pageRequest = PageRequest.of(page-1, 5);
        Page<PosterResponse> likePosters = posterRepository.getLikePosters(memberId, pageRequest);
        PageResponse<PosterResponse> pageResponse = new PageResponse<>(likePosters);
        return pageResponse;
    }

    public List<PosterResponse> getBestPosters() {
        List<PosterResponse> bestPosters = posterRepository.getBestPosters();
        return bestPosters;
    }

    public PageResponse<PosterResponse> getWritePosters(Integer page, Long memberId) {
        PageRequest pageRequest = PageRequest.of(page - 1, 5);
        Page<PosterResponse> writePosters = posterRepository.getWritePosters(memberId, pageRequest);
        PageResponse<PosterResponse> pageResponse = new PageResponse<>(writePosters);
        return pageResponse;
    }

    public PageResponse<PosterResponse> getPostersByWriteComments(Integer page, Long memberId) {
        PageRequest pageRequest = PageRequest.of(page - 1, 5);
        Page<PosterResponse> posters = posterRepository.getPostersByWriteComments(memberId, pageRequest);
        PageResponse<PosterResponse> pageResponse = new PageResponse<>(posters);
        return pageResponse;
    }


}
