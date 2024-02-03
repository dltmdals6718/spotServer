package com.example.spotserver.domain;

import com.example.spotserver.dto.request.PosterRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Poster {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member writer;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    private String title;
    private String content;

    @CreationTimestamp
    private LocalDateTime regDate;

    public void updatePoster(PosterRequest posterRequest) {
        this.title = posterRequest.getTitle();
        this.content = posterRequest.getContent();
    }
}
