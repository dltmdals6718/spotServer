package com.example.spotserver.controller;

import com.example.spotserver.config.auth.PrincipalDetails;
import com.example.spotserver.domain.Location;
import com.example.spotserver.domain.Member;
import com.example.spotserver.dto.request.ApproveRequest;
import com.example.spotserver.dto.request.LocationRequest;
import com.example.spotserver.dto.response.ApproveResponse;
import com.example.spotserver.dto.response.LocationResponse;
import com.example.spotserver.service.CommentService;
import com.example.spotserver.service.LocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
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

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {LocationController.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocationControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

    private Member member;
    private PrincipalDetails principalDetails;

    @BeforeAll
    public void beforeAll() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilter(new CharacterEncodingFilter("utf-8", true))
                .build();

        member = new Member();
        member.setId(1L);
        member.setName("닉네임");
        principalDetails = mock(PrincipalDetails.class);

        when(principalDetails.getMember())
                .thenReturn(member);

    }

    @BeforeEach
    public void beforeEach() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        context.setAuthentication(authentication);
    }

    @Test
    @DisplayName("장소 등록")
    void addLocation() throws Exception {

        //given
        Long locationId = 3L;

        LocationRequest locationRequestDTO = new LocationRequest();
        locationRequestDTO.setTitle("테스트 장소");
        locationRequestDTO.setAddress("주소");
        locationRequestDTO.setDescription("설명");
        locationRequestDTO.setLongitude(1D);
        locationRequestDTO.setLatitude(2D);


        Location location = LocationRequest.toEntity(locationRequestDTO);
        ObjectMapper objectMapper = new ObjectMapper();
        String locationRequestString = objectMapper.writeValueAsString(locationRequestDTO);
        MockMultipartFile locationRequest = new MockMultipartFile("locationRequest", null, MediaType.APPLICATION_JSON_VALUE, locationRequestString.getBytes());

        ArrayList<MultipartFile> files = new ArrayList<>();
        MockMultipartFile imgFile1 = new MockMultipartFile("files", "file1.png", MediaType.IMAGE_PNG_VALUE, "".getBytes());
        MockMultipartFile imgFile2 = new MockMultipartFile("files", "file2.png", MediaType.IMAGE_PNG_VALUE, "".getBytes());
        files.add(imgFile1);
        files.add(imgFile2);

        given(locationService.addLocation(location, files))
                .willReturn(locationId);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .multipart(HttpMethod.POST, "/locations")
                .file(locationRequest)
                .file(imgFile1)
                .file(imgFile2));

        //then
        verify(locationService, times(1))
                .addLocation(location, files);
        resultActions
                .andExpectAll(
                        status().is(HttpStatus.CREATED.value()),
                        jsonPath("$.locationId").value(locationId))
                .andDo(print());
    }

    @Test
    @DisplayName("단일 장소 조회")
    void getLocation() throws Exception {

        //given
        Long locationId = 3L;
        LocationResponse locationResponse = new LocationResponse();
        locationResponse.setLocationId(locationId);
        locationResponse.setLatitude(1.12);
        locationResponse.setLatitude(2.23);
        locationResponse.setTitle("제목");
        locationResponse.setAddress("주소");
        locationResponse.setDescription("설명");
        locationResponse.setRegDate(LocalDateTime.now().withNano(0));
        locationResponse.setLikeCnt(2L);

        given(locationService.getLocation(locationId))
                .willReturn(locationResponse);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.GET, "/locations/" + locationId));

        //then
        verify(locationService, times(1))
                .getLocation(locationId);
        resultActions
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.locationId").value(locationResponse.getLocationId()),
                        jsonPath("$.latitude").value(locationResponse.getLatitude()),
                        jsonPath("$.longitude").value(locationResponse.getLongitude()),
                        jsonPath("$.title").value(locationResponse.getTitle()),
                        jsonPath("$.address").value(locationResponse.getAddress()),
                        jsonPath("$.description").value(locationResponse.getDescription()),
                        jsonPath("$.regDate").value(locationResponse.getRegDate().toString()),
                        jsonPath("$.likeCnt").value(locationResponse.getLikeCnt()))
                .andDo(print());
    }

    @Test
    @DisplayName("장소 승인으로 변경")
    void approveLocation() throws Exception {

        //given
        Long locationId = 3L;

        ApproveRequest approveRequest = new ApproveRequest();
        approveRequest.setApprove(true);

        ObjectMapper objectMapper = new ObjectMapper();
        String approveRequestString = objectMapper.writeValueAsString(approveRequest);

        ApproveResponse approveResponse = new ApproveResponse();
        approveResponse.setLocationId(locationId);
        approveResponse.setApprove(true);

        given(locationService.updateApprove(locationId, approveRequest))
                .willReturn(approveResponse);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.PUT, "/locations/" + locationId + "/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(approveRequestString));

        //then
        verify(locationService, times(1))
                .updateApprove(locationId, approveRequest);
        resultActions
                .andExpectAll(
                        jsonPath("$.locationId").value(approveResponse.getLocationId()),
                        jsonPath("$.approve").value(approveRequest.getApprove()))
                .andDo(print());


    }

    @Test
    @DisplayName("장소 미승인으로 변경")
    void unapproveLocation() throws Exception {

        //given
        Long locationId = 3L;

        ApproveRequest approveRequest = new ApproveRequest();
        approveRequest.setApprove(false);

        ObjectMapper objectMapper = new ObjectMapper();
        String approveRequestString = objectMapper.writeValueAsString(approveRequest);

        ApproveResponse approveResponse = new ApproveResponse();
        approveResponse.setLocationId(locationId);
        approveResponse.setApprove(false);

        given(locationService.updateApprove(locationId, approveRequest))
                .willReturn(approveResponse);

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.PUT, "/locations/" + locationId + "/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(approveRequestString));

        //then
        verify(locationService, times(1))
                .updateApprove(locationId, approveRequest);
        resultActions
                .andExpectAll(
                        jsonPath("$.locationId").value(approveResponse.getLocationId()),
                        jsonPath("$.approve").value(approveRequest.getApprove()))
                .andDo(print());
    }

    @Test
    @DisplayName("장소 좋아요 등록")
    void addLike() throws Exception {

        //given
        Long locationId = 3L;

        doNothing()
                .when(locationService)
                .addLike(locationId, member.getId());

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.POST, "/locations/" + locationId + "/likes"));

        //then
        verify(locationService, times(1))
                .addLike(locationId, member.getId());
        resultActions
                .andExpectAll(
                        status().is(HttpStatus.CREATED.value()))
                .andDo(print());
    }

    @Test
    @DisplayName("장소 좋아요 취소")
    void deleteLike() throws Exception {

        //given
        Long locationId = 3L;

        doNothing()
                .when(locationService)
                .deleteLike(locationId, member.getId());

        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .request(HttpMethod.DELETE, "/locations/" + locationId + "/likes"));

        //then
        verify(locationService, times(1))
                .deleteLike(locationId, member.getId());
        resultActions
                .andExpectAll(
                        status().is(HttpStatus.NO_CONTENT.value()))
                .andDo(print());
    }
}
