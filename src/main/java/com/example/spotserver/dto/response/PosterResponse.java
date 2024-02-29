package com.example.spotserver.dto.response;

import com.example.spotserver.domain.Poster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PosterResponse {

    private Long posterId;
    private Long writerId;
    private String writerName;
    private String title;
    private String content;
    private LocalDateTime regDate;
    private Long likeCnt;
    private Long commentCnt;

    
}
