package com.example.spotserver.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.example.spotserver.domain.ImageStore;
import com.example.spotserver.domain.PosterImage;
import com.example.spotserver.domain.LocationImage;
import com.example.spotserver.repository.PosterImageRepository;
import com.example.spotserver.repository.LocationImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ImageFileService {

    private PosterImageRepository posterImageRepository;
    private LocationImageRepository locationImageRepository;
    private ImageStore imageStore;
    private AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Autowired
    public ImageFileService(PosterImageRepository posterImageRepository, LocationImageRepository locationImageRepository, ImageStore imageStore, AmazonS3Client amazonS3Client) {
        this.posterImageRepository = posterImageRepository;
        this.locationImageRepository = locationImageRepository;
        this.imageStore = imageStore;
        this.amazonS3Client = amazonS3Client;
    }

    public List<PosterImage> getPosterImageList(Long posterId) {
        List<PosterImage> posterImages = posterImageRepository.findByPosterId(posterId);
        return posterImages;
    }

    public List<LocationImage> getLocationImageList(Long locationId) {
        List<LocationImage> locationImages = locationImageRepository.findByLocationId(locationId);
        return locationImages;
    }

    public String getLocationImageUrl(Long locationImageId) {
        LocationImage locationImage = locationImageRepository.findById(locationImageId)
                .orElseThrow(() -> new NoSuchElementException());
        String imagefileName = locationImage.getStoreFileName();
        String imageUrl = amazonS3Client.getResourceUrl(bucket, imageStore.getLocationImgDir() + imagefileName);
        return imageUrl;
    }

    public String getPosterImageUrl(Long posterImageId) {
        PosterImage posterImage = posterImageRepository.findById(posterImageId)
                .orElseThrow(() -> new NoSuchElementException());
        String imagefileName = posterImage.getStoreFileName();
        String imageUrl = amazonS3Client.getResourceUrl(bucket, imageStore.getPosterImgDir() + imagefileName);
        return imageUrl;
    }

}
