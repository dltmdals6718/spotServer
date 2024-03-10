package com.example.spotserver.service;

import com.example.spotserver.domain.ImageStore;
import com.example.spotserver.domain.PosterImage;
import com.example.spotserver.domain.LocationImage;
import com.example.spotserver.repository.PosterImageRepository;
import com.example.spotserver.repository.LocationImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public ImageFileService(PosterImageRepository posterImageRepository, LocationImageRepository locationImageRepository, ImageStore imageStore) {
        this.posterImageRepository = posterImageRepository;
        this.locationImageRepository = locationImageRepository;
        this.imageStore = imageStore;
    }

    public List<PosterImage> getPosterImageList(Long posterId) {
        List<PosterImage> posterImages = posterImageRepository.findByPosterId(posterId);
        return posterImages;
    }

    public List<LocationImage> getLocationImageList(Long locationId) {
        List<LocationImage> locationImages = locationImageRepository.findByLocationId(locationId);
        return locationImages;
    }

    public Resource getLocationImage(Long locationImageId) throws MalformedURLException {
        LocationImage locationImage = locationImageRepository.findById(locationImageId)
                .orElseThrow(() -> new NoSuchElementException());
        String imagefileName = locationImage.getStoreFileName();
        UrlResource resource = new UrlResource("file:" + imageStore.getLocationImgFullPath(imagefileName));
        return resource;
    }

    public Resource getPosterImage(Long posterImageId) throws MalformedURLException {
        PosterImage posterImage = posterImageRepository.findById(posterImageId)
                .orElseThrow(() -> new NoSuchElementException());

        String imagefileName = posterImage.getStoreFileName();

        UrlResource resource = new UrlResource("file:" + imageStore.getPosterImgFullPath(imagefileName));

        return resource;
    }

    public void deletePosterImage(Long posterImageId) {
        PosterImage posterImage = posterImageRepository.findById(posterImageId)
                .orElseThrow(() -> new NoSuchElementException());

        String fullPath = imageStore.getPosterImgFullPath(posterImage.getStoreFileName());
        File file =  new File(fullPath);
        if(file.exists())
            file.delete();

        posterImageRepository.deleteById(posterImageId);
    }
}
