package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.Lecture;
import com.aslan.academymanagement.domain.Schedule;
import com.aslan.academymanagement.domain.ScheduleException;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Builder
public class LectureEventDto {
    private String id; // Schedule ID
    private String title;
    private String start;
    private String end;
    private String color;
    private Map<String, Object> extendedProps;

    // 기간(start~end) 내의 반복 일정을 계산하여 이벤트 리스트 반환 (예외 처리 포함)
    public static List<LectureEventDto> from(Lecture lecture, LocalDate queryStart, LocalDate queryEnd, List<ScheduleException> exceptions) {
        List<LectureEventDto> events = new ArrayList<>();

        // 강의 기간과 조회 기간의 교집합 구하기
        LocalDate effectiveStart = lecture.getStartDate().isAfter(queryStart) ? lecture.getStartDate() : queryStart;
        LocalDate effectiveEnd = lecture.getEndDate().isBefore(queryEnd) ? lecture.getEndDate() : queryEnd;

        if (effectiveStart.isAfter(effectiveEnd)) {
            return events; // 겹치는 기간 없음
        }

        // 예외 사항을 Map으로 변환 (Key: ScheduleID + OriginalDate)
        Map<String, ScheduleException> exceptionMap = exceptions.stream()
                .collect(Collectors.toMap(
                        ex -> ex.getSchedule().getId() + "_" + ex.getOriginalDate(),
                        ex -> ex,
                        (existing, replacement) -> existing // 중복 시 기존 것 유지
                ));

        for (Schedule schedule : lecture.getSchedules()) {
            for (LocalDate date = effectiveStart; !date.isAfter(effectiveEnd); date = date.plusDays(1)) {
                if (date.getDayOfWeek() == schedule.getDayOfWeek()) {
                    
                    // 예외 사항 체크
                    String key = schedule.getId() + "_" + date;

                    if (exceptionMap.containsKey(key)) {
                        ScheduleException ex = exceptionMap.get(key);
                        log.info("⚡️ 예외 일정 발견: {} (원래 날짜: {}) -> 변경 날짜: {}", key, ex.getOriginalDate(), ex.getNewDate());

                        if (ex.isCancelled()) {
                            log.info("🚫 휴강 처리됨: {}", key);
                            continue; // 휴강이면 건너뜀
                        }
                        // 변경된 날짜/시간 적용
                        LocalDate targetDate = ex.getNewDate() != null ? ex.getNewDate() : date;
                        LocalDateTime startDateTime = LocalDateTime.of(targetDate, ex.getNewStartTime() != null ? ex.getNewStartTime() : schedule.getStartTime());
                        LocalDateTime endDateTime = LocalDateTime.of(targetDate, ex.getNewEndTime() != null ? ex.getNewEndTime() : schedule.getEndTime());
                        
                        addEvent(events, lecture, schedule, startDateTime, endDateTime);
                    } else {
                        // 정상 일정
                        LocalDateTime startDateTime = LocalDateTime.of(date, schedule.getStartTime());
                        LocalDateTime endDateTime = LocalDateTime.of(date, schedule.getEndTime());
                        
                        addEvent(events, lecture, schedule, startDateTime, endDateTime);
                    }
                }
            }
        }
        return events;
    }

    private static void addEvent(List<LectureEventDto> events, Lecture lecture, Schedule schedule, LocalDateTime start, LocalDateTime end) {
        String subjectName = (lecture.getSubject() != null) ? lecture.getSubject().name() : "NONE";

        Map<String, Object> props = new HashMap<>();
        props.put("instructor", lecture.getTeacher().getName());
        props.put("lectureId", lecture.getId());

        events.add(LectureEventDto.builder()
                .id(String.valueOf(schedule.getId()))
                .title(lecture.getName())
                .start(start.toString())
                .end(end.toString())
                .color(getColorBySubject(subjectName))
                .extendedProps(props)
                .build());
    }

    private static String getColorBySubject(String subject) {
        if (subject == null) return "#6c757d";

        switch (subject) {
            case "KOREAN": return "#dc3545";
            case "ENGLISH": return "#28a745";
            case "MATH": return "#007bff";
            case "SCIENCE": return "#ffc107";
            default: return "#6c757d";
        }
    }
}
