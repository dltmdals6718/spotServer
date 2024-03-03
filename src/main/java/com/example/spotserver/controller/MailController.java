package com.example.spotserver.controller;


import com.example.spotserver.exception.ErrorCode;
import com.example.spotserver.exception.MailException;
import com.example.spotserver.service.MailSerivce;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/mails")
public class MailController {

    private MailSerivce mailSerivce;

    @Autowired
    public MailController(MailSerivce mailSerivce) {
        this.mailSerivce = mailSerivce;
    }

    @PostMapping("/certification")
    public ResponseEntity sendMail(@RequestParam String mail) throws MessagingException, MailException {

        if(!isValidMail(mail)) {
            throw new MailException(ErrorCode.NOT_VALID_MAIL);
        }

        mailSerivce.sendMailCertification(mail);

        return ResponseEntity
                .ok()
                .build();
    }

    public boolean isValidMail(String mail) {
        String emailPattern =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(mail);

        return matcher.matches();
    }


}
