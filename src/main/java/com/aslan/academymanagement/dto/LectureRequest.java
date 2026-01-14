package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.Lecture;
import com.aslan.academymanagement.domain.Schedule;
import com.aslan.academymanagement.domain.enums.LectureType;
import com.aslan.academymanagement.domain.enums.Subject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LectureRequest {
    private String name; // title -> name
    private LectureType type; // lectureType -> type
    private Subject subject;
    private BigDecimal defaultPrice;
    private Integer defaultDuration;
    private List<ScheduleRequest> schedules; // scheduleRequest -> schedules

    public Lecture toLecture() {
        return Lecture.builder()
                .name(name)
                .type(type)
                .subject(subject)
                .defaultPrice(defaultPrice != null ? defaultPrice : BigDecimal.ZERO)
                .defaultDuration(defaultDuration != null ? defaultDuration : 60)
                .isActive(true)
                .build();
    }

    public List<Schedule> toSchedules() {
        if (schedules == null) {
            return List.of();
        }
        return schedules.stream()
                .map(req -> Schedule.builder()
                        .dayOfWeek(req.getDayOfWeek())
                        .startTime(req.getStartTime())
                        .endTime(req.getEndTime())
                        .build())
                .toList();
    }
}
