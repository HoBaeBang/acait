package com.aslan.academymanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LectureScheduleRequest {

    @Schema(description = "요일", example = "MONDAY")
    private DayOfWeek dayOfWeek;

    @Schema(description = "시작 시간 (HH:mm:ss)", example = "14:30:00", type = "string")
    private LocalTime startTime;

    @Schema(description = "종료 시간 (HH:mm:ss)", example = "16:00:00", type = "string")
    private LocalTime endTime;
}
