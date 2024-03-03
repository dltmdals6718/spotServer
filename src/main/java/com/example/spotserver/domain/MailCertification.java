package com.example.spotserver.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
public class MailCertification {

    @Id
    private String mail;
    private Integer code;

    @CreationTimestamp
    private LocalDateTime regDate;

}
