package com.aslan.academymanagement.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "lecture_students",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_lecture_student",
                        columnNames = {"lecture_id", "student_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class LectureStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime registeredAt;

    @Builder
    public LectureStudent(Lecture lecture, Student student) {
        this.lecture = lecture;
        this.student = student;
    }
}
