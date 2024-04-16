package com.example.spotserver.dto.response;

import com.example.spotserver.domain.ImageStore;
import com.example.spotserver.domain.Member;
import com.example.spotserver.domain.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@Getter
@Setter
public class MemberResponse {

    private Long memberId;
    private String name;
    private Role role;
    private String memberImg;

    public static MemberResponse toDto(Member member, ImageStore imageStore) {
        MemberResponse memberResponse = new MemberResponse();
        memberResponse.memberId = member.getId();
        memberResponse.name = member.getName();
        memberResponse.role = member.getRole();


        String uri;
        String fileName;
        if (member.getMemberImg() != null) {
            fileName = member.getMemberImg().getStoreFileName();
            uri = imageStore.getMemberImgFullPath(fileName);
        } else // 기본 이미지 경로
            uri = "...";

        memberResponse.setMemberImg(uri);
        return memberResponse;
    }
}
