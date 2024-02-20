package com.example.spotserver.repository;

import com.example.spotserver.domain.MemberImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberImageRepository extends JpaRepository<MemberImage, Long> {

    Optional<MemberImage> findByStoreFileName(String storeFileName);
}
