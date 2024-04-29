package com.example.spotserver.Integration;


import com.amazonaws.services.s3.AmazonS3Client;
import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.ApproveRequest;
import com.example.spotserver.dto.response.LocationResponse;
import com.example.spotserver.dto.response.PageResponse;
import com.example.spotserver.exception.DuplicateException;
import com.example.spotserver.exception.ErrorCode;
import com.example.spotserver.exception.MailException;
import com.example.spotserver.repository.*;
import com.example.spotserver.service.LocationService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
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
import java.util.Optional;

@SpringBootTest
@Transactional
public class LocationTest {

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PosterRepository posterRepository;

    @Autowired
    private PosterImageRepository posterImageRepository;

    @Autowired
    private PosterLikeRepository posterLikeRepository;

    @Autowired
    private LocationImageRepository locationImageRepository;

    @Autowired
    private LocationLikeRepository locationLikeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private ImageStore imageStore;

    @Autowired
    private AmazonS3Client amazonS3Client;

    @Autowired
    private EntityManager em;

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
            imageStore.deleteLocationImage(locationImage);
        }

    }

    @Test
    @DisplayName("Location 삭제")
    void deleteLocation() throws IOException {

        //given
        Location location = new Location();
        location.setTitle("장소명");
        locationRepository.save(location);

        MockMultipartFile locationFile = new MockMultipartFile("locationImg", "locationImg.jpg", MediaType.IMAGE_JPEG_VALUE, "".getBytes());
        List<MultipartFile> locationFiles = new ArrayList<>();
        locationFiles.add(locationFile);
        List<LocationImage> locationImages = imageStore.storeLocationImages(locationFiles);
        for (LocationImage locationImage : locationImages) {
            locationImage.setLocation(location);
            locationImageRepository.save(locationImage);
        }

        Member member = new Member();
        memberRepository.save(member);

        LocationLike locationLike = new LocationLike();
        locationLike.setMember(member);
        locationLike.setLocation(location);
        locationLikeRepository.save(locationLike);

        Poster poster = new Poster();
        poster.setLocation(location);
        poster.setWriter(member);
        posterRepository.save(poster);

        MockMultipartFile posterFile = new MockMultipartFile("posterImg", "posterImg.jpg", MediaType.IMAGE_JPEG_VALUE, "".getBytes());
        List<MultipartFile> posterFiles = new ArrayList<>();
        posterFiles.add(posterFile);
        List<PosterImage> posterImages = imageStore.storePosterImages(posterFiles);
        for (PosterImage posterImage : posterImages) {
            posterImage.setPoster(poster);
            posterImageRepository.save(posterImage);
        }

        PosterLike posterLike = new PosterLike();
        posterLike.setMember(member);
        posterLike.setPoster(poster);
        posterLikeRepository.save(posterLike);

        Comment comment = new Comment();
        comment.setWriter(member);
        comment.setPoster(poster);
        commentRepository.save(comment);

        CommentLike commentLike = new CommentLike();
        commentLike.setMember(member);
        commentLikeRepository.save(commentLike);

        //when
        em.flush();
        em.clear();
        Long deleteLocationId = location.getId();
        locationService.deleteLocation(deleteLocationId);

        //then
        Assertions
                .assertThat(locationRepository.findById(deleteLocationId))
                .isNotPresent();
        Assertions
                .assertThat(locationImageRepository.findByLocationId(deleteLocationId))
                .isEmpty();
        Assertions
                .assertThat(locationLikeRepository.findByLocationAndMember(location, member))
                .isNotPresent();

        Assertions
                .assertThat(posterRepository.findByLocation(location))
                .isEmpty();
        Assertions
                .assertThat(posterLikeRepository.findByPosterAndMember(poster, member))
                .isNotPresent();

        Assertions
                .assertThat(commentRepository.findByPoster(poster))
                .isEmpty();
        Assertions
                .assertThat(commentLikeRepository.findByCommentAndMember(comment, member))
                .isNotPresent();

        Assertions
                .assertThat(amazonS3Client.doesObjectExist(imageStore.getBucket(), imageStore.getLocationImgDir() + locationImages.get(0).getStoreFileName()))
                .isFalse();
        Assertions
                .assertThat(amazonS3Client.doesObjectExist(imageStore.getBucket(), imageStore.getPosterImgDir() + posterImages.get(0).getStoreFileName()))
                .isFalse();

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
        em.flush();
        em.clear();

        //then
        Location updateLocation = locationRepository.findById(location.getId())
                .orElseThrow(() -> new NoSuchElementException());
        Assertions
                .assertThat(updateLocation.getApprove())
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
        em.flush();
        em.clear();

        //then
        Location updateLocation = locationRepository.findById(location.getId())
                .orElseThrow(() -> new NoSuchElementException());
        Assertions
                .assertThat(updateLocation.getApprove())
                .isFalse();
    }

    @Test
    @DisplayName("좋아요 등록")
    void addLike() throws DuplicateException {

        //given
        Location location = new Location();
        location.setTitle("테스트 장소");
        locationRepository.save(location);

        Member member = new Member();
        member.setName("회원");
        memberRepository.save(member);

        //when
        locationService.addLike(location.getId(), member.getId());
        em.flush();
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

        Member member = new Member();
        member.setName("회원");
        memberRepository.save(member);

        //when
        locationService.addLike(location.getId(), member.getId());

        Assertions
                .assertThatThrownBy(() -> locationService.addLike(location.getId(), member.getId()))
                .isInstanceOf(DuplicateException.class)
                .hasMessage(ErrorCode.DUPLICATE_LIKE.getMessage());

    }

    @Test
    @DisplayName("좋아요 삭제")
    void deleteLike() {

        //given
        Location location = new Location();
        location.setTitle("테스트 장소");
        locationRepository.save(location);

        Member member = new Member();
        member.setName("회원");
        memberRepository.save(member);

        //when
        LocationLike locationLike = new LocationLike();
        locationLike.setLocation(location);
        locationLike.setMember(member);
        locationLikeRepository.save(locationLike);

        em.clear();
        locationService.deleteLike(location.getId(), member.getId());
        em.flush();
        em.clear();

        //then
        Location findLocation = locationRepository.findById(location.getId())
                .orElseThrow(() -> new NoSuchElementException());

        Assertions
                .assertThat(findLocation.getLocationLikes().size())
                .isEqualTo(0);

    }

    @Test
    @DisplayName("존재하지 않는 좋아요 삭제")
    void deleteNoExistLike() {

        //given
        Location location = new Location();
        location.setTitle("테스트 장소");
        locationRepository.save(location);

        Member member = new Member();
        member.setName("회원");
        memberRepository.save(member);

        Assertions
                .assertThatThrownBy(() -> locationService.deleteLike(location.getId(), member.getId()))
                .isInstanceOf(NoSuchElementException.class);

    }

    @Test
    @DisplayName("좋아요 장소 조회")
    void getLikeLocations() {

        //given
        Member member = new Member();
        member.setName("회원");
        memberRepository.save(member);

        Location location1 = new Location();
        Location location2 = new Location();
        locationRepository.save(location1);
        locationRepository.save(location2);

        LocationLike locationLike1 = new LocationLike();
        locationLike1.setLocation(location1);
        locationLike1.setMember(member);
        locationLikeRepository.save(locationLike1);

        LocationLike locationLike2 = new LocationLike();
        locationLike2.setLocation(location2);
        locationLike2.setMember(member);
        locationLikeRepository.save(locationLike2);

        //when
        em.flush();
        em.clear();
        PageResponse<LocationResponse> likeLocations = locationService.getLikeLocations(1, member.getId());

        //then
        List<LocationResponse> results = likeLocations.getResults();
        Assertions
                .assertThat(results.size())
                .isEqualTo(2);
    }

}