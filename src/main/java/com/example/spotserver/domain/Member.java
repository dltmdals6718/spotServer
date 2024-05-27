package com.example.spotserver.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String loginId;
    private String loginPwd;

    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    @CreationTimestamp
    private LocalDateTime regDate;

    @OneToOne(mappedBy = "member", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private MemberImage memberImg;

    @Enumerated(EnumType.STRING)
    private MemberType type;

    private String mail;

    private Long snsId;

    @OneToMany(mappedBy = "writer", cascade = {CascadeType.REMOVE})
    private List<Poster> posters;

    @OneToMany(mappedBy = "member", cascade = {CascadeType.REMOVE})
    private List<PosterLike> posterLikes;

    @OneToMany(mappedBy = "writer", cascade = {CascadeType.REMOVE})
    private List<Comment> comments;

    @OneToMany(mappedBy = "member", cascade = {CascadeType.REMOVE})
    private List<CommentLike> commentLikes;

    @OneToMany(mappedBy = "member", cascade = {CascadeType.REMOVE})
    private List<LocationLike> locationLikes;

}
