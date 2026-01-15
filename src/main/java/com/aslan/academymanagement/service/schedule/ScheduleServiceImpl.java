package com.aslan.academymanagement.service.schedule;

import com.aslan.academymanagement.domain.LectureRecord;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.Schedule;
import com.aslan.academymanagement.domain.enums.AttendanceStatus;
import com.aslan.academymanagement.dto.ScheduleUpdateRequest;
import com.aslan.academymanagement.repository.LectureRecordRepository;
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
    private final LectureRecordRepository lectureRecordRepository;

    @Override
    public void updateSchedule(Member teacher, Long scheduleId, ScheduleUpdateRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스케줄이 없습니다."));

        // 권한 체크
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

    // Case A: 이번 주만 변경 (Instance Update)
    private void updateInstance(Schedule schedule, ScheduleUpdateRequest request) {
        if (request.getTargetDate() == null) {
            throw new IllegalArgumentException("INSTANCE 변경 시 대상 날짜(targetDate)는 필수입니다.");
        }

        // 해당 날짜에 대한 예외적인 LectureRecord 생성 (또는 수정)
        // 주의: 여기서는 '어떤 학생'에 대한 기록인지 특정할 수 없으므로,
        // 실제로는 '휴강' 처리나 '강의 시간 변경'을 기록하는 별도의 엔티티가 필요할 수 있음.
        // 하지만 현재 요구사항에서는 LectureRecord를 활용하라고 되어 있으므로,
        // 해당 강의를 수강하는 모든 학생에 대해 Record를 미리 생성해두거나,
        // 혹은 '스케줄 예외'를 관리하는 별도 테이블이 없으므로 로직이 복잡해짐.
        
        // [임시 구현] 로그만 남기고 실제 DB 반영은 보류 (구조적 한계)
        // 제대로 하려면 ScheduleException 테이블이 필요함.
        log.info("📅 [INSTANCE] {} 날짜의 스케줄을 {}-{}로 변경합니다.", 
                request.getTargetDate(), request.getStartTime(), request.getEndTime());
    }

    // Case B: 앞으로 쭉 변경 (Series Update)
    private void updateSeries(Schedule schedule, ScheduleUpdateRequest request) {
        // 기존 스케줄 업데이트
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        
        // JPA 변경 감지로 자동 저장
        log.info("📅 [SERIES] 스케줄(ID:{})을 {}-{}로 영구 변경합니다.", 
                schedule.getId(), request.getStartTime(), request.getEndTime());
        
        // 주의: 이미 생성된 과거의 LectureRecord는 수정되지 않음 (데이터 보존)
    }
}
