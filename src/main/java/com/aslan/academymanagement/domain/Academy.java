package com.aslan.academymanagement.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "academies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Academy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "academy_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "invite_code", unique = true)
    private String inviteCode;

    @Column(name = "max_members", nullable = false)
    private Integer maxMembers;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Academy(String name) {
        this.name = name;
        this.maxMembers = 3; // 기본값 3명
        this.inviteCode = UUID.randomUUID().toString().substring(0, 8); // 랜덤 초대 코드 생성
    }

    public void updateMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }
}
