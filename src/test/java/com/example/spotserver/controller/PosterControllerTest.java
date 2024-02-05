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
import java.util.List;

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

    @BeforeAll
    public void beforeAll() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilter(new CharacterEncodingFilter("utf-8", true))
                .build();

        member = mock(Member.class);
        principalDetails = mock(PrincipalDetails.class);

        when(principalDetails.getMember())
                .thenReturn(member);

        when(member.getId())
                .thenReturn(1L);
        when(member.getName())
                .thenReturn("name");

    }

    @BeforeEach
    public void beforeEach() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        context.setAuthentication(authentication);
    }

    @Test
    @DisplayName("게시글 작성")
    void addPoster() {
    }

    @Test
    @DisplayName("장소 게시글 전체 조회")
    void getLocationPosters() {
    }

    @Test
    @DisplayName("특정 게시글 조회")
    void getPoster() {
    }

    @Test
    @DisplayName("게시글 수정")
    void updatePoster() throws Exception {

        Poster poster = new Poster();
        poster.setId(1L);
        poster.setWriter(member);
        poster.setTitle("수정 전 제목");
        poster.setContent("수정 전 내용");
        poster.setRegDate(LocalDateTime.now());

        PosterRequest posterRequestDto = new PosterRequest();
        posterRequestDto.setTitle("수정 후 제목");
        posterRequestDto.setContent("수정 후 내용");

        poster.updatePoster(posterRequestDto);
        PosterResponse posterResponse = PosterResponse.toDto(poster);


        ObjectMapper objectMapper = new ObjectMapper();
        String posterRequestString = objectMapper.writeValueAsString(posterRequestDto);

        MockMultipartFile posterRequest = new MockMultipartFile("posterRequest", null, MediaType.APPLICATION_JSON_VALUE, posterRequestString.getBytes(StandardCharsets.UTF_8));

        List<MultipartFile> addFiles = new ArrayList<>();
        MockMultipartFile addFiles1 = new MockMultipartFile("addFiles", null, MediaType.MULTIPART_FORM_DATA_VALUE, new FileInputStream("/Users/lsm/Desktop/testImg/1.png"));
        MockMultipartFile addFiles2 = new MockMultipartFile("addFiles", null, MediaType.MULTIPART_FORM_DATA_VALUE, new FileInputStream("/Users/lsm/Desktop/testImg/2.png"));
        addFiles.add(addFiles1);
        addFiles.add(addFiles2);

        List<Long> deleteFilesId = new ArrayList<>();
        deleteFilesId.add(1L);
        deleteFilesId.add(2L);
        String deleteFilesIdString = objectMapper.writeValueAsString(deleteFilesId);
        MockMultipartFile deleteFilesIdMock = new MockMultipartFile("deleteFilesId", null, MediaType.APPLICATION_JSON_VALUE, deleteFilesIdString.getBytes());


        given(posterService.updatePoster(poster.getId(), posterRequestDto, addFiles, deleteFilesId, member))
                .willReturn(posterResponse);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .multipart(HttpMethod.PUT, "/posters/" + poster.getId())
                .file(posterRequest)
                .file(addFiles1)
                .file(addFiles2)
                .file(deleteFilesIdMock)
                .characterEncoding("utf-8")
                .with(csrf()));

        verify(posterService, times(1))
                .updatePoster(poster.getId(), posterRequestDto, addFiles, deleteFilesId, member);

        resultActions
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.posterId").value(poster.getId()),
                        jsonPath("$.writerId").value(member.getId()),
                        jsonPath("$.title").value(posterRequestDto.getTitle()),
                        jsonPath("$.content").value(posterRequestDto.getContent()),
                        jsonPath("$.regDate").value(poster.getRegDate().toString()))
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
                .deletePoster(poster.getId(), member);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.DELETE, "/posters/" + poster.getId()));

        verify(posterService, times(1))
                .deletePoster(poster.getId(), member);

        resultActions
                .andExpectAll(
                        status().is(HttpStatus.NO_CONTENT.value())
                )
                .andDo(print());
    }


}