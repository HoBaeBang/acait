package com.aslan.academymanagement.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "schedule_exceptions", indexes = {
        @Index(name = "idx_exception_schedule", columnList = "schedule_id"),
        @Index(name = "idx_exception_original_date", columnList = "original_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ScheduleException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exception_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(name = "original_date", nullable = false)
    private LocalDate originalDate; // 원래 수업 날짜

    @Column(name = "new_date")
    private LocalDate newDate; // 변경된 날짜 (휴강이면 null 가능)

    @Column(name = "new_start_time")
    private LocalTime newStartTime;

    @Column(name = "new_end_time")
    private LocalTime newEndTime;

    @Column(name = "is_cancelled", nullable = false)
    private boolean isCancelled; // 휴강 여부

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ScheduleException(Schedule schedule, LocalDate originalDate, LocalDate newDate, LocalTime newStartTime, LocalTime newEndTime, boolean isCancelled) {
        this.schedule = schedule;
        this.originalDate = originalDate;
        this.newDate = newDate;
        this.newStartTime = newStartTime;
        this.newEndTime = newEndTime;
        this.isCancelled = isCancelled;
    }
}
