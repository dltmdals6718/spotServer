package com.example.spotserver.repository;

import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.MemberType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByLoginId(String loginId);
    boolean existsByName(String name);
    boolean existsByMail(String mail);

    Optional<Member> findByLoginId(String loginId);
    Optional<Member> findByTypeAndSnsId(MemberType memberType, Long id);

}
