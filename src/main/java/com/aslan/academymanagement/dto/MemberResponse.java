package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.enums.MemberStatus;
import com.aslan.academymanagement.domain.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MemberResponse {
    private Long id;
    private String googleEmail;
    private String name;
    private String phone;
    private String contactEmail;
    private String picture;
    private Role role;
    private MemberStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;

    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .googleEmail(member.getGoogleEmail())
                .name(member.getName())
                .phone(member.getPhone())
                .contactEmail(member.getContactEmail())
                .picture(member.getPicture())
                .role(member.getRole())
                .status(member.getStatus())
                .createdAt(member.getCreatedAt())
                .approvedAt(member.getApprovedAt())
                .build();
    }
}
