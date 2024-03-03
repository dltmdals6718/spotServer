package com.example.spotserver.service;

import com.example.spotserver.domain.MailCertification;
import com.example.spotserver.exception.ErrorCode;
import com.example.spotserver.exception.MailException;
import com.example.spotserver.repository.MailCertificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MailSerivce {

    private MailCertificationRepository mailCertificationRepository;
    private JavaMailSender javaMailSender;

    @Autowired
    public MailSerivce(MailCertificationRepository mailCertificationRepository, JavaMailSender javaMailSender) {
        this.mailCertificationRepository = mailCertificationRepository;
        this.javaMailSender = javaMailSender;
    }

    @Transactional
    public void sendMailCertification(String mail) throws MessagingException, MailException {


        Optional<MailCertification> optionalMail = mailCertificationRepository.findByMail(mail);
        if(optionalMail.isPresent()) {
            LocalDateTime now = LocalDateTime.now();
            if(now.isBefore(optionalMail.get().getRegDate().plusMinutes(5)))
                throw new MailException(ErrorCode.FAIL_MAIL_CERTIFICATION_REQUEST);
        }

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        mimeMessageHelper.setTo(mail);
        mimeMessageHelper.setSubject("Photo Spot 회원가입 인증 번호");

        int min = 10000;
        int max = 99999;
        Integer code = Integer.valueOf((int) (Math.random() * (max - min + 1)) + min);
        mimeMessageHelper.setText("인증 번호 : " + code.toString(), true);
        javaMailSender.send(mimeMessage);

        if(optionalMail.isPresent())
            mailCertificationRepository.deleteByMail(mail);

        MailCertification mailCertification = new MailCertification();
        mailCertification.setMail(mail);
        mailCertification.setCode(code);
        mailCertificationRepository.save(mailCertification);

    }
}
