package com.aslan.academymanagement.service.schedule;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.Schedule;
import com.aslan.academymanagement.domain.ScheduleException;
import com.aslan.academymanagement.dto.ScheduleUpdateRequest;
import com.aslan.academymanagement.repository.ScheduleExceptionRepository;
import com.aslan.academymanagement.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleExceptionRepository scheduleExceptionRepository;

    @Override
    public void updateSchedule(Member teacher, Long scheduleId, ScheduleUpdateRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스케줄이 없습니다."));

        if (!schedule.getLecture().getTeacher().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("해당 스케줄에 대한 권한이 없습니다.");
        }

        if ("INSTANCE".equals(request.getScope())) {
            updateInstance(schedule, request);
        } else if ("SERIES".equals(request.getScope())) {
            updateSeries(schedule, request);
        } else {
            throw new IllegalArgumentException("잘못된 변경 범위(scope)입니다.");
        }
    }

    private void updateInstance(Schedule schedule, ScheduleUpdateRequest request) {
        if (request.getTargetDate() == null) {
            throw new IllegalArgumentException("INSTANCE 변경 시 대상 날짜(targetDate)는 필수입니다.");
        }

        log.info("🔍 [DEBUG] ScheduleUpdateRequest - targetDate: {}, newDate: {}, startTime: {}, endTime: {}",
                request.getTargetDate(), request.getNewDate(), request.getStartTime(), request.getEndTime());

        // ScheduleException 생성 및 저장
        ScheduleException exception = ScheduleException.builder()
                .schedule(schedule)
                .originalDate(request.getTargetDate())
                .newDate(request.getNewDate() != null ? request.getNewDate() : request.getTargetDate())
                .newStartTime(request.getStartTime())
                .newEndTime(request.getEndTime())
                .isCancelled(false) // 시간 변경이므로 휴강 아님
                .build();

        scheduleExceptionRepository.save(exception);
        
        log.info("📅 [INSTANCE] 예외 등록 완료 - Original: {} -> New: {} {}-{}",
                exception.getOriginalDate(), exception.getNewDate(), exception.getNewStartTime(), exception.getNewEndTime());
    }

    private void updateSeries(Schedule schedule, ScheduleUpdateRequest request) {
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        
        log.info("📅 [SERIES] 스케줄(ID:{})을 {}-{}로 영구 변경합니다.",
                schedule.getId(), request.getStartTime(), request.getEndTime());
    }
}
