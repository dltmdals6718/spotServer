package com.example.spotserver.Integration;

import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.PosterConditionRequest;
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
import java.time.LocalDateTime;
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
    private CommentRepository commentRepository;

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

    @Test
    @DisplayName("게시글 최신순 조회")
    void getPostersByRecent() {

        //given
        Member writer = new Member();
        writer.setName("홍길동");
        memberRepository.save(writer);

        Location newLocation = new Location();
        locationRepository.save(newLocation);

        Long posterCnt = 7L;
        for (int i = 1; i <= posterCnt; i++) {
            Poster newPoster = new Poster();
            newPoster.setLocation(newLocation);
            newPoster.setWriter(writer);
            newPoster.setLocation(newLocation);
            newPoster.setTitle(i + "번째 제목");
            posterRepository.save(newPoster);
            newPoster.setRegDate(newPoster.getRegDate().plusHours(i));
        }

        PosterConditionRequest conditionRequest = new PosterConditionRequest();
        Integer page = 1;
        Integer size = posterCnt.intValue();
        conditionRequest.setPage(page);
        conditionRequest.setSize(size);
        conditionRequest.setSort("recent");

        //when
        em.flush();
        PageResponse<PosterResponse> pageResponse = posterService.getLocationPosters(newLocation.getId(), conditionRequest);
        PageInfo pageInfo = pageResponse.getPageInfo();
        List<PosterResponse> posters = pageResponse.getResults();

        //then
        Assertions
                .assertThat(posters.size())
                .isEqualTo(size);
        Assertions
                .assertThat(pageInfo.getSize())
                .isEqualTo(size);
        Assertions
                .assertThat(pageInfo.getPage())
                .isEqualTo(page);

        for (int i = 0; i < posters.size(); i++) {
            Assertions
                    .assertThat(posters.get(i).getTitle())
                    .isEqualTo((posterCnt-i) + "번째 제목");
        }
    }

    @Test
    @DisplayName("게시글 좋아요순 조회")
    void getPostersByLike() {

        //given
        Member writer = new Member();
        writer.setName("홍길동");
        memberRepository.save(writer);

        Location newLocation = new Location();
        locationRepository.save(newLocation);

        Long posterCnt = 7L;
        for (int i = 1; i <= posterCnt; i++) {
            Poster newPoster = new Poster();
            newPoster.setLocation(newLocation);
            newPoster.setWriter(writer);
            newPoster.setLocation(newLocation);
            newPoster.setTitle(i + "번째 제목");
            posterRepository.save(newPoster);

            for(int j=1; j<=i; j++) {
                Member liker = new Member();
                memberRepository.save(liker);

                PosterLike posterLike = new PosterLike();
                posterLike.setMember(liker);
                posterLike.setPoster(newPoster);
                posterLikeRepository.save(posterLike);
            }

        }

        PosterConditionRequest conditionRequest = new PosterConditionRequest();
        Integer page = 1;
        Integer size = posterCnt.intValue();
        conditionRequest.setPage(page);
        conditionRequest.setSize(size);
        conditionRequest.setSort("like");

        //when
        em.flush();
        PageResponse<PosterResponse> pageResponse = posterService.getLocationPosters(newLocation.getId(), conditionRequest);
        PageInfo pageInfo = pageResponse.getPageInfo();
        List<PosterResponse> posters = pageResponse.getResults();

        //then
        Assertions
                .assertThat(posters.size())
                .isEqualTo(size);
        Assertions
                .assertThat(pageInfo.getSize())
                .isEqualTo(size);
        Assertions
                .assertThat(pageInfo.getPage())
                .isEqualTo(page);

        for (int i = 0; i < posters.size(); i++) {
            Assertions
                    .assertThat(posters.get(i).getLikeCnt())
                    .isEqualTo((posterCnt-i));
        }

    }

    @Test
    @DisplayName("게시글 제목 검색")
    void getPostersBySearchTitle() {

        //given
        Member writer = new Member();
        writer.setName("홍길동");
        memberRepository.save(writer);

        Location newLocation = new Location();
        locationRepository.save(newLocation);

        Long posterCnt = 7L;
        for (int i = 1; i <= posterCnt; i++) {
            Poster newPoster = new Poster();
            newPoster.setLocation(newLocation);
            newPoster.setWriter(writer);
            newPoster.setLocation(newLocation);
            newPoster.setTitle(i + "번째 제목");
            posterRepository.save(newPoster);
        }

        PosterConditionRequest conditionRequest = new PosterConditionRequest();
        Integer page = 1;
        Integer size = posterCnt.intValue();
        String search = posterCnt.toString();
        conditionRequest.setPage(page);
        conditionRequest.setSize(size);
        conditionRequest.setSearch(search);

        //when
        em.flush();
        PageResponse<PosterResponse> pageResponse = posterService.getLocationPosters(newLocation.getId(), conditionRequest);
        PageInfo pageInfo = pageResponse.getPageInfo();
        List<PosterResponse> posters = pageResponse.getResults();

        //then
        Assertions
                .assertThat(posters.size())
                .isEqualTo(1);
        Assertions
                .assertThat(pageInfo.getSize())
                .isEqualTo(size);
        Assertions
                .assertThat(pageInfo.getPage())
                .isEqualTo(page);
        Assertions
                .assertThat(posters.get(0).getTitle())
                .isEqualTo(search + "번째 제목");
    }

    @Test
    @DisplayName("게시글 내용 검색")
    void getPostersBySearchContent() {

        //given
        Member writer = new Member();
        writer.setName("홍길동");
        memberRepository.save(writer);

        Location newLocation = new Location();
        locationRepository.save(newLocation);

        Long posterCnt = 7L;
        for (int i = 1; i <= posterCnt; i++) {
            Poster newPoster = new Poster();
            newPoster.setLocation(newLocation);
            newPoster.setWriter(writer);
            newPoster.setLocation(newLocation);
            newPoster.setContent(i + "번째 내용");
            posterRepository.save(newPoster);
        }

        PosterConditionRequest conditionRequest = new PosterConditionRequest();
        Integer page = 1;
        Integer size = posterCnt.intValue();
        String search = posterCnt.toString();
        conditionRequest.setPage(page);
        conditionRequest.setSize(size);
        conditionRequest.setSearch(search);

        //when
        em.flush();
        PageResponse<PosterResponse> pageResponse = posterService.getLocationPosters(newLocation.getId(), conditionRequest);
        PageInfo pageInfo = pageResponse.getPageInfo();
        List<PosterResponse> posters = pageResponse.getResults();

        //then
        Assertions
                .assertThat(posters.size())
                .isEqualTo(1);
        Assertions
                .assertThat(pageInfo.getSize())
                .isEqualTo(size);
        Assertions
                .assertThat(pageInfo.getPage())
                .isEqualTo(page);
        Assertions
                .assertThat(posters.get(0).getContent())
                .isEqualTo(search + "번째 내용");
    }

    @Test
    @DisplayName("내가 작성한 게시글들 조회")
    void getWritePosters() {

        //given
        Member writer1 = new Member();
        memberRepository.save(writer1);

        Poster writePoster1 = new Poster();
        writePoster1.setTitle("1번째 게시글");
        writePoster1.setWriter(writer1);
        writePoster1.setLocation(location);
        posterRepository.save(writePoster1);
        writePoster1.setRegDate(writePoster1.getRegDate().plusHours(2));

        Poster writePoster2 = new Poster();
        writePoster2.setTitle("2번째 게시글");
        writePoster2.setWriter(writer1);
        writePoster2.setLocation(location);
        posterRepository.save(writePoster2);
        writePoster2.setRegDate(writePoster2.getRegDate().plusHours(1));

        //when
        PageResponse<PosterResponse> writePosters = posterService.getWritePosters(1, writer1.getId());
        List<PosterResponse> results = writePosters.getResults();
        PageInfo pageInfo = writePosters.getPageInfo();

        //then
        Assertions
                .assertThat(pageInfo.getTotalElements())
                .isEqualTo(2);

        for(int i=0; i< results.size(); i++) {
            Assertions
                    .assertThat(results.get(i).getTitle())
                    .isEqualTo((i+1) + "번째 게시글");
        }
    }

    @Test
    @DisplayName("댓글 단 게시글 조회")
    void getPostersByWriteComments() {

        //given
        Member commentWriter = new Member();
        memberRepository.save(commentWriter);

        Poster poster1 = new Poster();
        poster1.setWriter(member);
        poster1.setLocation(location);
        poster1.setTitle("1번째 게시글");
        posterRepository.save(poster1);

        Comment comment1 = new Comment();
        comment1.setContent("첫번째 댓글");
        comment1.setWriter(commentWriter);
        comment1.setPoster(poster1);
        commentRepository.save(comment1);
        comment1.setRegDate(comment1.getRegDate().plusHours(15));

        Comment comment2 = new Comment();
        comment2.setContent("두번째 댓글");
        comment2.setWriter(commentWriter);
        comment2.setPoster(poster1);
        commentRepository.save(comment2);
        comment2.setRegDate(comment2.getRegDate().plusHours(1));

        Poster poster2 = new Poster();
        poster2.setTitle("2번째 게시글");
        poster2.setWriter(member);
        poster2.setLocation(location);
        posterRepository.save(poster2);

        Comment comment3 = new Comment();
        comment3.setContent("세번째 댓글");
        comment3.setWriter(commentWriter);
        comment3.setPoster(poster2);
        commentRepository.save(comment3);
        comment3.setRegDate(comment3.getRegDate().plusHours(10));

        Comment comment4 = new Comment();
        comment4.setContent("네번째 댓글");
        comment4.setWriter(commentWriter);
        comment4.setPoster(poster2);
        commentRepository.save(comment4);
        comment3.setRegDate(comment4.getRegDate().plusHours(10));


        //when
        PageResponse<PosterResponse> postersByWriteComments = posterService.getPostersByWriteComments(1, commentWriter.getId());
        List<PosterResponse> results = postersByWriteComments.getResults();

        //then
        //중복 없이 게시글 출력
        Assertions
                .assertThat(results.size())
                .isEqualTo(2);

        // 최근에 작성한 댓글의 게시글이 최상단
        for(int i=0; i<results.size(); i++) {
            Assertions
                    .assertThat(results.get(i).getTitle())
                    .isEqualTo((i+1) + "번째 게시글");
        }

    }


}
