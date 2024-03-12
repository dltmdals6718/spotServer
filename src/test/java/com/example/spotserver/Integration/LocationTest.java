package com.example.spotserver.Integration;


import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.ApproveRequest;
import com.example.spotserver.exception.DuplicateException;
import com.example.spotserver.exception.MailException;
import com.example.spotserver.repository.*;
import com.example.spotserver.service.LocationService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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
    LocationLikeRepository locationLikeRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ImageStore imageStore;

    @Autowired
    EntityManager em;

    Member member;

    @BeforeEach
    void init() {
        member = new Member();
        member.setName("테스터");
        member.setRole(Role.USER);
        memberRepository.save(member);
    }


    @Test
    @DisplayName("Location 등록")
    void addLocation() throws IOException {

        //given
        String title = "테스트 장소";
        Double longitude = 1.1;
        Double latitude = 2.2;
        String address = "주소";
        String description = "설명";

        Location location = new Location();
        location.setTitle(title);
        location.setLongitude(longitude);
        location.setLatitude(latitude);
        location.setAddress(address);
        location.setDescription(description);

        String imgName1 = "abc1.jpg";
        String imgName2 = "abc2.jpg";

        MockMultipartFile file1 = new MockMultipartFile(imgName1, imgName1, MediaType.IMAGE_JPEG_VALUE, "img".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(imgName2, imgName2, MediaType.IMAGE_JPEG_VALUE, "img".getBytes());
        List<MultipartFile> files = new ArrayList<>();
        files.add(file1);
        files.add(file2);

        //when
        locationService.addLocation(location, files);

        //then
        Location findLocation = locationRepository.findById(location.getId())
                .orElseThrow(() -> new NoSuchElementException());
        List<LocationImage> locationImages = locationImageRepository.findByLocationId(findLocation.getId());

        Assertions
                .assertThat(findLocation.getTitle())
                .isEqualTo(title);
        Assertions
                .assertThat(findLocation.getLongitude())
                .isEqualTo(longitude);
        Assertions
                .assertThat(findLocation.getLatitude())
                .isEqualTo(latitude);
        Assertions
                .assertThat(findLocation.getAddress())
                .isEqualTo(address);
        Assertions
                .assertThat(findLocation.getDescription())
                .isEqualTo(description);

        Assertions
                .assertThat(locationImages.size())
                .isEqualTo(2);

        for (LocationImage locationImage : locationImages) {
            File file = new File(imageStore.getLocationImgFullPath(locationImage.getStoreFileName()));

            if(file.exists())
                file.delete();
            else
                throw new FileNotFoundException();
        }

    }

    @Test
    @DisplayName("Location 삭제")
    void deleteLocation() throws IOException {

        //given
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

        //when
        locationService.deleteLocation(location.getId());

        //then
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

    @Test
    @DisplayName("Location 승인으로 변경")
    void approveLocation() {

        //given
        Location location = new Location();
        location.setTitle("테스트 장소");

        locationRepository.save(location);

        ApproveRequest approveRequest = new ApproveRequest();
        approveRequest.setApprove(true);

        //when
        locationService.updateApprove(location.getId(), approveRequest);

        //then
        Assertions
                .assertThat(location.getApprove())
                .isTrue();
    }

    @Test
    @DisplayName("Location 미승인으로 변경")
    void unapproveLocation() {

        //given
        Location location = new Location();
        location.setTitle("테스트 장소");
        location.setApprove(true);

        locationRepository.save(location);

        ApproveRequest approveRequest = new ApproveRequest();
        approveRequest.setApprove(false);

        //when
        locationService.updateApprove(location.getId(), approveRequest);

        //then
        Assertions
                .assertThat(location.getApprove())
                .isFalse();
    }

    @Test
    @DisplayName("좋아요 등록")
    void addLike() throws DuplicateException {

        //given
        Location location = new Location();
        location.setTitle("테스트 장소");
        locationRepository.save(location);

        //when
        locationService.addLike(location.getId(), member.getId());

        em.clear();

        //then
        Location findLocation = locationRepository.findById(location.getId())
                .orElseThrow(() -> new NoSuchElementException());

        Assertions
                .assertThat(findLocation.getLocationLikes().size())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("중복된 좋아요 등록")
    void duplicateLike() throws DuplicateException {

        //given
        Location location = new Location();
        location.setTitle("테스트 장소");
        locationRepository.save(location);

        //when
        locationService.addLike(location.getId(), member.getId());

        Assertions
                .assertThatThrownBy(() -> locationService.addLike(location.getId(), member.getId()))
                .isInstanceOf(DuplicateException.class);

    }

    @Test
    @DisplayName("좋아요 삭제")
    void deleteLike() {

        //given
        Location location = new Location();
        location.setTitle("테스트 장소");
        locationRepository.save(location);

        //when
        LocationLike locationLike1 = new LocationLike();
        locationLike1.setLocation(location);
        locationLike1.setMember(member);
        locationLikeRepository.save(locationLike1);

        em.clear();

        locationService.deleteLike(location.getId(), member.getId());


        //then
        Location findLocation = locationRepository.findById(location.getId())
                .orElseThrow(() -> new NoSuchElementException());

        Assertions
                .assertThat(findLocation.getLocationLikes().size())
                .isEqualTo(1);

    }

    @Test
    @DisplayName("존재하지 않는 좋아요 삭제")
    void deleteNoExistLike() {

        //given
        Location location = new Location();
        location.setTitle("테스트 장소");
        locationRepository.save(location);

        Assertions
                .assertThatThrownBy(() -> locationService.deleteLike(location.getId(), member.getId()))
                .isInstanceOf(NoSuchElementException.class);

    }

}