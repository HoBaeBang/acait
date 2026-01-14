package com.aslan.academymanagement.domain;

import com.aslan.academymanagement.domain.enums.Division;
import com.aslan.academymanagement.domain.enums.Grade;
import com.aslan.academymanagement.domain.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", nullable = false)
    private Academy academy;

    @Column(nullable = false)
    private String name;

    @Column
    private String school;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Column(name = "birth_date", length = 4)
    private String birthDate;

    @Column(name = "parent_phone", nullable = false)
    private String parentPhone;

    @Column(name = "parent_email")
    private String parentEmail;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StudentStatus status = StudentStatus.ATTENDING;

    @Column(name = "discharge_date")
    private LocalDate dischargeDate;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
