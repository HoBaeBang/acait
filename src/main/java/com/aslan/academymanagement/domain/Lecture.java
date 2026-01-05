package com.aslan.academymanagement.domain;

import com.aslan.academymanagement.domain.enums.LectureType;
import com.aslan.academymanagement.domain.enums.Subject;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lectures")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LectureType lectureType; // 학원/과외 구분

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Subject subject;

    // 양방향 매핑: Lecture가 삭제되면 스케줄도 같이 삭제됨 (CascadeType.ALL, orphanRemoval = true)
    @OneToMany(mappedBy = "lecture",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<LectureSchedule> schedules = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 연관관계 편의 메서드
    public void addSchedule(LectureSchedule schedule) {
        this.schedules.add(schedule);
        schedule.setLecture(this);
    }
}
