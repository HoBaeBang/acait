package com.aslan.academymanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleUpdateRequest {

    @Schema(description = "변경할 시작 시간", example = "15:00:00", type = "string")
    @NotNull
    private LocalTime startTime;

    @Schema(description = "변경할 종료 시간", example = "16:30:00", type = "string")
    @NotNull
    private LocalTime endTime;

    @Schema(description = "변경 범위 (INSTANCE: 이번만, SERIES: 앞으로 쭉)", allowableValues = {"INSTANCE", "SERIES"})
    @NotNull
    private String scope;

    @Schema(description = "변경 대상 날짜 (INSTANCE일 때 필수)", example = "2026-01-15")
    private LocalDate targetDate;

    @Schema(description = "변경된 날짜 (날짜 자체를 변경할 때 사용)", example = "2026-01-16")
    private LocalDate newDate; // 추가: 날짜 변경 지원
}
