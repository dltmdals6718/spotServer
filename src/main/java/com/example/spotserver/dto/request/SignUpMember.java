package com.example.spotserver.dto.request;

import com.example.spotserver.domain.Member;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "올바른 닉네임을 입력해주세요.")
    private String name;


    public void setName(String name) {
        this.name = name.trim();
    }

    public Member toEntity(SignUpMember memberRequest) {
        Member member = new Member();
        member.setLoginId(memberRequest.loginId);
        member.setLoginPwd(memberRequest.loginPwd);
        member.setName(memberRequest.name);
        return member;
    }
}
