package com.example.spotserver.domain;

import jakarta.persistence.*;
import lombok.Data;

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

    @OneToMany(cascade = CascadeType.REMOVE)
    private List<LocationLike> locationLikes;

}
