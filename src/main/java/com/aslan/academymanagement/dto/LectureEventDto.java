package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.Lecture;
import com.aslan.academymanagement.domain.LectureSchedule;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class LectureEventDto {
    private String title;
    private String start; // ISO8601 형식 (YYYY-MM-DDTHH:mm:ss)
    private String end;
    private String color; // 과목별 색상

    // 강의와 스케줄을 받아서 이번 주(This Week)의 이벤트 리스트로 변환하는 메서드
    public static List<LectureEventDto> from(Lecture lecture) {
        List<LectureEventDto> events = new ArrayList<>();
        
        // 이번 주 월요일 날짜 구하기 (기준점)
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        for (LectureSchedule schedule : lecture.getSchedules()) {
            // 해당 요일의 날짜 계산
            LocalDate targetDate = monday.with(TemporalAdjusters.nextOrSame(schedule.getDayOfWeek()));
            
            // 날짜 + 시간 합치기
            LocalDateTime startDateTime = LocalDateTime.of(targetDate, schedule.getStartTime());
            LocalDateTime endDateTime = LocalDateTime.of(targetDate, schedule.getEndTime());

            events.add(LectureEventDto.builder()
                    .title(lecture.getTitle())
                    .start(startDateTime.toString())
                    .end(endDateTime.toString())
                    .color(getColorBySubject(lecture.getSubject().name()))
                    .build());
        }
        return events;
    }

    private static String getColorBySubject(String subject) {
        switch (subject) {
            case "KOREAN": return "#dc3545"; // 빨강
            case "ENGLISH": return "#28a745"; // 초록
            case "MATH": return "#007bff"; // 파랑
            case "SCIENCE": return "#ffc107"; // 노랑
            default: return "#6c757d"; // 회색
        }
    }
}
