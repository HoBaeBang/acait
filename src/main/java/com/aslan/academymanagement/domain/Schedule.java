package com.aslan.academymanagement.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "schedules")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @Column(name = "day_of_week", nullable = false)
    // DayOfWeek Enum을 그대로 쓸지, String으로 변환할지 결정 필요.
    // 명세서에는 VARCHAR(3) 'MON', 'TUE' 등으로 되어 있음.
    // @Enumerated(EnumType.STRING)을 쓰면 'MONDAY' 전체가 저장됨.
    // 여기서는 편의상 STRING 유지하되, 필요시 컨버터 사용 가능.
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    public boolean isValidTime() {
        return startTime.isBefore(endTime);
    }
}
