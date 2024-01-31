package com.example.spotserver.controller;

import com.example.spotserver.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.junit.jupiter.api.Assertions.*;
@WebMvcTest(value = {PosterController.class, TestSecurityConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PosterControllerTest {

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
    void updatePoster() {

    }

    @Test
    @DisplayName("게시글 삭제")
    void deletePoster() {

    }


}