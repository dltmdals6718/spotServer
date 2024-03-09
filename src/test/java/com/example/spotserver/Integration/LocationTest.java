package com.example.spotserver.Integration;


import com.example.spotserver.domain.*;
import com.example.spotserver.repository.*;
import com.example.spotserver.service.LocationService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

@SpringBootTest
@Transactional
public class LocationTest {

    @Autowired
    LocationService locationService;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    PosterRepository posterRepository;

    @Autowired
    CommentLikeRepository commentLikeRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    PosterLikeRepository posterLikeRepository;

    @Autowired
    PosterImageRepository posterImageRepository;

    @Autowired
    LocationImageRepository locationImageRepository;

    @Autowired
    ImageStore imageStore;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("Location 삭제")
    void deleteLocation() throws IOException {

        Location location = new Location();
        location.setTitle("테스트 장소");
        locationRepository.save(location);

        LocationImage locationImage = new LocationImage();
        locationImage.setLocation(location);
        String locationImgStoreName = "a-b-c.jpg";
        String locationImgUploadName = "abc.jpg";
        locationImage.setStoreFileName(locationImgStoreName);
        locationImage.setUploadFileName(locationImgUploadName);
        locationImageRepository.save(locationImage);

        File locationImgFile = new File(imageStore.getLocationImgFullPath(locationImgStoreName));
        locationImgFile.createNewFile();

        Poster poster = new Poster();
        poster.setTitle("게시글1");
        poster.setLocation(location);
        posterRepository.save(poster);

        PosterImage posterImage = new PosterImage();
        posterImage.setPoster(poster);
        String posterImgStoreName = "d-e-f.jpg";
        String posterImgUploadName = "def.jpg";
        posterImage.setStoreFileName(posterImgStoreName);
        posterImage.setUploadFileName(posterImgUploadName);
        posterImageRepository.save(posterImage);

        File posterImgFIle = new File(imageStore.getPosterImgFullPath(posterImgStoreName));
        posterImgFIle.createNewFile();

        PosterLike posterLike = new PosterLike();
        posterLike.setPoster(poster);
        posterLikeRepository.save(posterLike);

        Poster poster2 = new Poster();
        poster2.setTitle("게시글2");
        poster2.setLocation(location);
        posterRepository.save(poster2);

        Comment comment = new Comment();
        comment.setPoster(poster2);
        comment.setContent("댓글");
        commentRepository.save(comment);

        CommentLike commentLike = new CommentLike();
        commentLike.setComment(comment);
        commentLikeRepository.save(commentLike);

        em.clear();

        locationService.deleteLocation(location.getId());

        Assertions
                .assertThat(locationRepository.findById(location.getId()))
                .isNotPresent();

        Assertions
                .assertThat(locationImgFile.exists())
                .isFalse();

        Assertions
                .assertThat(locationImageRepository.findByLocationId(location.getId()))
                .isEmpty();

        Assertions
                .assertThat(posterRepository.findByLocation(location))
                .isEmpty();

        Assertions
                .assertThat(posterImgFIle.exists())
                .isFalse();

        Assertions
                .assertThat(posterLikeRepository.findById(posterLike.getId()))
                .isNotPresent();

        Assertions
                .assertThat(commentRepository.findById(comment.getId()))
                .isNotPresent();

        Assertions
                .assertThat(commentLikeRepository.findById(commentLike.getId()))
                .isNotPresent();
    }


}