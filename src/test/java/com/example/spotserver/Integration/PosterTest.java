package com.example.spotserver.Integration;

import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.PosterRequest;
import com.example.spotserver.dto.response.PageResponse;
import com.example.spotserver.dto.response.PosterResponse;
import com.example.spotserver.exception.DuplicateException;
import com.example.spotserver.exception.PermissionException;
import com.example.spotserver.repository.*;
import com.example.spotserver.service.PosterService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@SpringBootTest
@Transactional
public class PosterTest {

    @Autowired
    private PosterService posterService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PosterRepository posterRepository;

    @Autowired
    private PosterImageRepository posterImageRepository;

    @Autowired
    private PosterLikeRepository posterLikeRepository;

    @Autowired
    private ImageStore imageStore;

    @Autowired
    private EntityManager em;

    private Member member;
    private Location location;
    private Poster poster;

    @BeforeEach
    void init() {
        member = new Member();
        member.setName("테스터");
        member.setRole(Role.USER);
        memberRepository.save(member);

        location = new Location();
        location.setTitle("테스트 장소");
        locationRepository.save(location);

        poster = new Poster();
        poster.setWriter(member);
        poster.setLocation(location);

        PosterImage posterImage = new PosterImage();
        String storeFileName = UUID.randomUUID() + ".jpg";
        posterImage.setPoster(poster);
        posterImage.setStoreFileName(storeFileName);

        List<PosterImage> posterImages = new ArrayList<>();
        posterImages.add(posterImage);
        poster.setPosterImages(posterImages);
        posterRepository.save(poster);

        PosterLike posterLike = new PosterLike();
        posterLike.setMember(member);
        posterLike.setPoster(poster);
        posterLikeRepository.save(posterLike);

    }

    @AfterEach
    void clearFile() {

        Poster findPoster = posterRepository.findById(poster.getId())
                .orElseThrow(() -> new NoSuchElementException());

        List<PosterImage> posterImages = findPoster.getPosterImages();

        for (PosterImage posterImage : posterImages) {
            String posterImgFullPath = imageStore.getPosterImgFullPath(posterImage.getStoreFileName());
            File file = new File(posterImgFullPath);
            if (file.exists())
                file.delete();
        }
    }

    @Test
    @DisplayName("게시글 작성")
    void addPoster() throws IOException {

        //given
        PosterRequest posterRequest = new PosterRequest();
        posterRequest.setTitle("게시글 제목");
        posterRequest.setContent("게시글 내용");
        Poster poster = PosterRequest.toEntity(posterRequest);

        String imgFileName1 = "image1.jpg";
        String imgFileName2 = "image2.jpg";
        MockMultipartFile file1 = new MockMultipartFile("img1", imgFileName1, MediaType.IMAGE_JPEG_VALUE, "img".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("img2", imgFileName2, MediaType.IMAGE_JPEG_VALUE, "img".getBytes());
        List<MultipartFile> files = new ArrayList<>();
        files.add(file1);
        files.add(file2);

        //when
        posterService.addPoster(poster, files, location.getId(), member.getId());
        em.clear();

        //then
        Poster findPoster = posterRepository.findById(poster.getId())
                .orElseThrow(() -> new NoSuchElementException());

        Assertions
                .assertThat(findPoster.getTitle())
                .isEqualTo(poster.getTitle());
        Assertions
                .assertThat(findPoster.getContent())
                .isEqualTo(poster.getContent());
        Assertions
                .assertThat(findPoster.getWriter().getId())
                .isEqualTo(member.getId());
        Assertions
                .assertThat(findPoster.getPosterImages().size())
                .isEqualTo(2);

        List<PosterImage> posterImages = posterImageRepository.findByPosterId(poster.getId());
        for (PosterImage posterImage : posterImages) {
            File file = new File(imageStore.getPosterImgFullPath(posterImage.getStoreFileName()));

            Assertions
                    .assertThat(file.exists())
                    .isTrue();

            if (file.exists())
                file.delete();

        }

    }

    @Test
    @DisplayName("게시글 수정")
    void updatePoster() throws PermissionException, IOException {

        //given
        PosterRequest posterRequest = new PosterRequest();
        posterRequest.setTitle("수정된 제목");
        posterRequest.setContent("수정된 내용");

        MockMultipartFile file1 = new MockMultipartFile("img1", "img1.jpg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("img2", "img2.jpg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes());
        List<MultipartFile> addFiles = new ArrayList<>();
        addFiles.add(file1);
        addFiles.add(file2);

        List<Long> deleteFilesId = new ArrayList<>();
        List<PosterImage> posterImages = poster.getPosterImages();
        for (PosterImage posterImage : posterImages) {
            deleteFilesId.add(posterImage.getId());
        }

        //when
        posterService.updatePoster(poster.getId(), posterRequest, addFiles, deleteFilesId, member.getId());

        //then
        Poster findPoster = posterRepository.findById(poster.getId())
                .orElseThrow(() -> new NoSuchElementException());

        Assertions
                .assertThat(findPoster.getPosterImages().size())
                .isEqualTo(2);

        Assertions
                .assertThat(findPoster.getTitle())
                .isEqualTo(posterRequest.getTitle());

        Assertions
                .assertThat(findPoster.getContent())
                .isEqualTo(posterRequest.getContent());
    }

    @Test
    @DisplayName("게시글 좋아요 등록")
    void addLike() throws DuplicateException {

        Member testMember = new Member();
        testMember.setName("좋아요 누를 예정인 사람");
        memberRepository.save(testMember);

        //given & when
        posterService.addLike(poster.getId(), testMember.getId());

        em.flush();
        em.clear();


        //then
        Poster findPoster = posterRepository.findById(poster.getId())
                .orElseThrow(() -> new NoSuchElementException());

        List<PosterLike> posterLikes = findPoster.getPosterLikes();

        Assertions
                .assertThat(posterLikes.size())
                .isEqualTo(2);

        Assertions
                .assertThat(posterLikes.get(1).getMember().getId())
                .isEqualTo(testMember.getId());

    }

    @Test
    @DisplayName("게시글 좋아요 취소")
    void deleteLike() {

        //given & when
        posterService.deleteLike(poster.getId(), member.getId());

        em.flush();
        em.clear();

        //then
        Assertions
                .assertThat(posterLikeRepository.findByPosterAndMember(poster, member))
                .isNotPresent();
        
    }

    @Test
    @DisplayName("좋아요 게시글 조회")
    void getLikePosters() {

        //given
        Member posterLiker = new Member();
        memberRepository.save(posterLiker);

        Poster poster1 = new Poster();
        poster1.setWriter(member);
        Poster poster2 = new Poster();
        poster2.setWriter(member);
        posterRepository.save(poster1);
        posterRepository.save(poster2);

        PosterLike posterLike1 = new PosterLike();
        posterLike1.setPoster(poster1);
        posterLike1.setMember(posterLiker);
        posterLikeRepository.save(posterLike1);

        PosterLike posterLike2 = new PosterLike();
        posterLike2.setPoster(poster2);
        posterLike2.setMember(posterLiker);
        posterLikeRepository.save(posterLike2);

        //when
        em.flush();
        em.clear();
        PageResponse<PosterResponse> likePosters = posterService.getLikePosters(1, posterLiker.getId());

        //then
        List<PosterResponse> results = likePosters.getResults();
        Assertions
                .assertThat(results.size())
                .isEqualTo(2);
    }
}
