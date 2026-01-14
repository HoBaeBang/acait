package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.Schedule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponse {
    private Long id;

    @Schema(description = "요일", example = "MONDAY")
    DayOfWeek dayOfWeek;

    @Schema(description = "시작 시간 (HH:mm:ss)", example = "14:30:00", type = "string")
    LocalTime startTime;

    @Schema(description = "종료 시간 (HH:mm:ss)", example = "16:00:00", type = "string")
    LocalTime endTime;


    public static ScheduleResponse from(Schedule schedule) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .build();
    }
}
