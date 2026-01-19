package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.LectureRecord;
import com.aslan.academymanagement.domain.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class SettlementDetailResponse {
    private LocalDate date;
    private String lectureName;
    private String studentName;
    private BigDecimal amount;
    private AttendanceStatus status;

    public static SettlementDetailResponse from(LectureRecord record) {
        return SettlementDetailResponse.builder()
                .date(record.getDate())
                .lectureName(record.getLecture().getName())
                .studentName(record.getStudent().getName())
                .amount(record.getLecture().getDefaultPrice())
                .status(record.getAttendanceStatus())
                .build();
    }
}
