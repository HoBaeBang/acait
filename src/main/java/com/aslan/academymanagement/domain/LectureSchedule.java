package com.aslan.academymanagement.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "lecture_schedules")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek; // 자바 표준 java.time.DayOfWeek 사용 (MONDAY, TUESDAY...)

    @Column(nullable = false)
    private LocalTime startTime; // 날짜 없이 시간만 저장 (예: 14:30)

    @Column(nullable = false)
    private LocalTime endTime;   // 날짜 없이 시간만 저장 (예: 16:00)

    // 편의 메서드: 시간 유효성 검증
    public boolean isValidTime() {
        return startTime.isBefore(endTime);
    }
}
