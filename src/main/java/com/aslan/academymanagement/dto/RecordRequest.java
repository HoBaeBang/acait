package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.enums.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordRequest {

    @NotNull(message = "강의 ID는 필수입니다.")
    private Long lectureId;

    @NotNull(message = "학생 ID는 필수입니다.")
    private Long studentId;

    @NotNull(message = "날짜는 필수입니다.")
    private LocalDate date;

    @Schema(description = "실제 시작 시간", example = "14:30:00", type = "string")
    private LocalTime actualStartTime;

    @NotNull(message = "출석 상태는 필수입니다.")
    private AttendanceStatus attendanceStatus;

    private String dailyLog;

    @Schema(description = "교재 정보 (JSON)", example = "{\"title\": \"수학의 정석\", \"page\": \"10-20\"}")
    private Map<String, Object> materialInfo;

    @Schema(description = "시간 충돌 시 강제 저장 여부", defaultValue = "false")
    private boolean forceUpdate;
}
