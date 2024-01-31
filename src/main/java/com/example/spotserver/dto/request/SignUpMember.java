package com.example.spotserver.dto.request;

import com.example.spotserver.domain.Member;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
public class SignUpMember {

    @NotEmpty(message = "아이디를 비울 수 없습니다.")
    private String loginId;

    @NotEmpty(message = "비밀번호를 비울 수 없습니다.")
    private String loginPwd;

    @NotEmpty(message = "닉네임을 비울 수 없습니다.")
    private String name;


    public Member toEntity(SignUpMember memberRequest) {
        Member member = new Member();
        member.setLoginId(memberRequest.loginId);
        member.setLoginPwd(memberRequest.loginPwd);
        member.setName(memberRequest.name);
        return member;
    }
}
