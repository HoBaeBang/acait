package com.aslan.academymanagement.domain;

import com.aslan.academymanagement.domain.enums.MemberStatus;
import com.aslan.academymanagement.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    // 구글 로그인 식별자 (수정 불가)
    @Column(name = "google_email", nullable = false, unique = true)
    private String googleEmail;

    // 사용자 실명
    @Column(nullable = false)
    private String name;

    // 연락처 (필수)
    @Column(nullable = false)
    private String phone;

    // 시스템 알림 수신용 이메일
    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column
    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column
    private String provider; // google

    @Column
    private String providerId; // sub

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime approvedAt;

    @Builder
    public Member(String googleEmail, String name, String phone, String contactEmail, String picture, Role role, MemberStatus status, String provider, String providerId) {
        this.googleEmail = googleEmail;
        this.name = name;
        this.phone = phone;
        this.contactEmail = contactEmail;
        this.picture = picture;
        this.role = role;
        this.status = status;
        this.provider = provider;
        this.providerId = providerId;
    }

    public Member update(String name, String picture) {
        this.name = name;
        this.picture = picture;
        return this;
    }

    public void approve() {
        this.status = MemberStatus.ACTIVE;
        this.approvedAt = LocalDateTime.now();
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}
