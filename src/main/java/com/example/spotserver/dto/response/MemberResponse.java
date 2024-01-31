package com.example.spotserver.dto.response;

import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
public class MemberResponse {

    private Long memberId;
    private String name;
    private Role role;

    public static MemberResponse toDto(Member member) {
        MemberResponse memberResponse = new MemberResponse();
        memberResponse.memberId = member.getId();
        memberResponse.name = member.getName();
        memberResponse.role = member.getRole();
        return memberResponse;
    }
}
