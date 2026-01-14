package com.aslan.academymanagement.domain;

import com.aslan.academymanagement.domain.enums.Division;
import com.aslan.academymanagement.domain.enums.Grade;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long id;

    // 학번 (ES001, MS001 등) - 비즈니스 키
    @Column(name = "student_number", nullable = false, unique = true)
    private String studentNumber;

    @Column(nullable = false)
    private String name;

    @Column
    private String school;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Grade grade;

    // 학부모 페이지 조회 인증용 (생일 4자리)
    @Column(name = "birth_date", length = 4)
    private String birthDate;

    // 학부모 알림 발송용 (필수)
    @Column(name = "parent_phone", nullable = false)
    private String parentPhone;

    @Column(name = "parent_email")
    private String parentEmail;

    @Column(columnDefinition = "TEXT")
    private String memo;

    // Division은 요구사항 명세서에는 없지만, 기존 로직 유지를 위해 일단 둠 (필요 시 삭제)
    @Enumerated(EnumType.STRING)
    private Division division;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
