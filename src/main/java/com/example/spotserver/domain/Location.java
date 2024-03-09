package com.example.spotserver.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Location {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double latitude;
    private Double longitude;
    private String title;
    private String address;
    private String description;

    @CreationTimestamp
    private LocalDateTime regDate;

    @Column(columnDefinition = "TINYINT(1) DEFAULT false")
    private Boolean approve = false;

    @OneToMany(mappedBy = "location", cascade = CascadeType.REMOVE)
    private List<LocationLike> locationLikes = new ArrayList<>();

    @OneToMany(mappedBy = "location", cascade = CascadeType.REMOVE)
    private List<Poster> posters = new ArrayList<>();

    @OneToMany(mappedBy = "location", cascade = CascadeType.REMOVE)
    private List<LocationImage> locationImages = new ArrayList<>();
}
