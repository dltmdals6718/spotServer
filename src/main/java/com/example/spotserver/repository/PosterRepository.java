package com.example.spotserver.repository;

import com.example.spotserver.domain.Location;
import com.example.spotserver.domain.Poster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PosterRepository extends JpaRepository<Poster, Long>, PosterRepositoryCustom {

}
