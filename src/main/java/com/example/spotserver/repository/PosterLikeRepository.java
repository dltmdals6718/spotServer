package com.example.spotserver.repository;

import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.Poster;
import com.example.spotserver.domain.PosterLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PosterLikeRepository extends JpaRepository<PosterLike, Long> {

    boolean existsPosterLikeByPosterAndMember(Poster poster, Member member);
    Optional<PosterLike> findByPosterAndMember(Poster poster, Member member);
}
