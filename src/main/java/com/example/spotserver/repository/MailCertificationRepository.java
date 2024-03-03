package com.example.spotserver.repository;

import com.example.spotserver.domain.MailCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailCertificationRepository extends JpaRepository<MailCertification, String> {

    Optional<MailCertification> findByMail(String mail);
    void deleteByMail(String mail);

}
