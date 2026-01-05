package com.aslan.academymanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record LectureScheduleRequest(
        @Schema(description = "요일", example = "MONDAY")
        DayOfWeek dayOfWeek,

        @Schema(description = "시작 시간 (HH:mm:ss)", example = "14:30:00", type = "string")
        LocalTime startTime,

        @Schema(description = "종료 시간 (HH:mm:ss)", example = "16:00:00", type = "string")
        LocalTime endTime
) {
}
