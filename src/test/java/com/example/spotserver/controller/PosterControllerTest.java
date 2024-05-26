package com.example.spotserver.controller;

import com.example.spotserver.config.auth.PrincipalDetails;
import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.Poster;
import com.example.spotserver.dto.request.PosterRequest;
import com.example.spotserver.dto.response.PosterResponse;
import com.example.spotserver.service.PosterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {PosterController.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PosterControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private PosterService posterService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private Member member;
    private PrincipalDetails principalDetails;

    private ObjectMapper objectMapper;

    @BeforeAll
    public void beforeAll() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilter(new CharacterEncodingFilter("utf-8", true))
                .build();

        member = new Member();
        member.setId(1L);
        member.setName("name");
        principalDetails = mock(PrincipalDetails.class);

        when(principalDetails.getMember())
                .thenReturn(member);


        objectMapper = new ObjectMapper();

    }

    @BeforeEach
    public void beforeEach() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        context.setAuthentication(authentication);
    }

    @Test
    @DisplayName("게시글 작성")
    void addPoster() throws Exception {

        //given
        Long locationId = 2L;

        PosterRequest posterRequestDto = new PosterRequest();
        posterRequestDto.setTitle("게시글 제목");
        posterRequestDto.setContent("게시글 내용");

        Poster poster = PosterRequest.toEntity(posterRequestDto);

        String posterRequestString = objectMapper.writeValueAsString(posterRequestDto);
        MockMultipartFile posterRequest = new MockMultipartFile("posterRequest", null, MediaType.APPLICATION_JSON_VALUE, posterRequestString.getBytes(StandardCharsets.UTF_8));

        List<MultipartFile> files = new ArrayList<>();
        MockMultipartFile file1 = new MockMultipartFile("files", "file1.png", MediaType.IMAGE_PNG_VALUE, "".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "file2.png", MediaType.IMAGE_PNG_VALUE, "".getBytes());
        files.add(file1);
        files.add(file2);

        Long posterId = 3L;
        given(posterService.addPoster(poster, files, locationId, member.getId()))
                .willReturn(posterId);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .multipart(HttpMethod.POST, "/locations/" + locationId + "/posters")
                .file(posterRequest)
                .file(file1)
                .file(file2));


        //then
        verify(posterService, times(1))
                .addPoster(poster, files, locationId, member.getId());

        resultActions
                .andExpectAll(
                        status().is(HttpStatus.CREATED.value()),
                        jsonPath("$.posterId").value(posterId))
                .andDo(print());

    }

    @Test
    @DisplayName("특정 게시글 조회")
    void getPoster() throws Exception {

        //given
        Long posterId = 3L;
        PosterResponse posterResponse = new PosterResponse();
        posterResponse.setPosterId(posterId);
        posterResponse.setTitle("제목");
        posterResponse.setContent("내용");
        posterResponse.setRegDate(LocalDateTime.now().withNano(0));
        posterResponse.setWriterId(member.getId());
        posterResponse.setWriterName(member.getName());
        posterResponse.setLikeCnt(1L);
        posterResponse.setCommentCnt(2L);

        given(posterService.getPoster(posterId))
                .willReturn(posterResponse);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.GET, "/posters/" + posterId));


        //then
        verify(posterService, times(1))
                .getPoster(posterId);
        resultActions
                .andExpectAll(
                        status().is(HttpStatus.OK.value()),
                        jsonPath("$.posterId").value(posterResponse.getPosterId()),
                        jsonPath("$.writerId").value(posterResponse.getWriterId()),
                        jsonPath("$.writerName").value(posterResponse.getWriterName()),
                        jsonPath("$.title").value(posterResponse.getTitle()),
                        jsonPath("$.content").value(posterResponse.getContent()),
                        jsonPath("$.regDate").value(posterResponse.getRegDate().toString()),
                        jsonPath("$.likeCnt").value(posterResponse.getLikeCnt()),
                        jsonPath("$.commentCnt").value(posterResponse.getCommentCnt()))
                .andDo(print());

    }

    @Test
    @DisplayName("게시글 수정")
    void updatePoster() throws Exception {

        //given
        Long posterId = 3L;
        PosterRequest posterRequestDto = new PosterRequest();
        posterRequestDto.setTitle("수정 후 제목");
        posterRequestDto.setContent("수정 후 내용");

        String posterRequestString = objectMapper.writeValueAsString(posterRequestDto);
        MockMultipartFile posterRequest = new MockMultipartFile("posterRequest", null, MediaType.APPLICATION_JSON_VALUE, posterRequestString.getBytes(StandardCharsets.UTF_8));

        List<MultipartFile> addFilesList = new ArrayList<>();
        MockMultipartFile addFiles1 = new MockMultipartFile("addFiles", null, MediaType.IMAGE_PNG_VALUE, "".getBytes());
        MockMultipartFile addFiles2 = new MockMultipartFile("addFiles", null, MediaType.IMAGE_PNG_VALUE, "".getBytes());
        addFilesList.add(addFiles1);
        addFilesList.add(addFiles2);

        List<Long> deleteFilesIdList = new ArrayList<>();
        deleteFilesIdList.add(1L);
        deleteFilesIdList.add(2L);
        String deleteFilesIdString = objectMapper.writeValueAsString(deleteFilesIdList);
        MockMultipartFile deleteFilesId = new MockMultipartFile("deleteFilesId", null, MediaType.APPLICATION_JSON_VALUE, deleteFilesIdString.getBytes());

        doNothing()
                .when(posterService)
                .updatePoster(posterId, posterRequestDto, addFilesList, deleteFilesIdList, member.getId());

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .multipart(HttpMethod.PUT, "/posters/" + posterId)
                .file(posterRequest)
                .file(addFiles1)
                .file(addFiles2)
                .file(deleteFilesId)
                .characterEncoding("utf-8")
                .with(csrf()));

        //then
        verify(posterService, times(1))
                .updatePoster(posterId, posterRequestDto, addFilesList, deleteFilesIdList, member.getId());
        resultActions
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.posterId").value(posterId))
                .andDo(print());

    }

    @Test
    @DisplayName("게시글 삭제")
    void deletePoster() throws Exception {

        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Poster poster = new Poster();
        poster.setWriter(member);
        poster.setId(1L);

        doNothing()
                .when(posterService)
                .deletePoster(poster.getId(), member.getId());

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.DELETE, "/posters/" + poster.getId()));

        verify(posterService, times(1))
                .deletePoster(poster.getId(), member.getId());

        resultActions
                .andExpectAll(
                        status().is(HttpStatus.NO_CONTENT.value()))
                .andDo(print());
    }

    @Test
    @DisplayName("좋아요 등록")
    void addLike() throws Exception {

        //given
        Long posterId = 2L;

        doNothing()
                .when(posterService)
                .addLike(posterId, member.getId());

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, "/posters/" + posterId + "/likes"));

        //then
        verify(posterService, times(1))
                .addLike(posterId, member.getId());
        resultActions
                .andExpectAll(
                        status().is(HttpStatus.CREATED.value()))
                .andDo(print());
    }

    @Test
    @DisplayName("좋아요 삭제")
    void deleteLike() throws Exception {

        //given
        Long posterId = 2L;

        doNothing()
                .when(posterService)
                .deleteLike(posterId, member.getId());

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.DELETE, "/posters/" + posterId + "/likes"));

        //then
        verify(posterService, times(1))
                .deleteLike(posterId, member.getId());
        resultActions
                .andExpectAll(
                        status().is(HttpStatus.NO_CONTENT.value()))
                .andDo(print());

    }

    @Test
    @DisplayName("좋아요 개수 조회")
    void getLikes() throws Exception {

        //given
        Long posterId = 2L;
        Long likeCnt = 4L;

        Map m = new HashMap<>();
        m.put("likeCnt", likeCnt);
        given(posterService.getLikes(posterId))
                .willReturn(m);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.GET, "/posters/" + posterId + "/likes"));

        //then
        verify(posterService, times(1))
                .getLikes(posterId);
        resultActions
                .andExpectAll(
                        jsonPath("$.likeCnt").value(likeCnt))
                .andDo(print());

    }


}