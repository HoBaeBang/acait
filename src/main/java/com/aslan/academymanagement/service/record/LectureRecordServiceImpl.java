package com.aslan.academymanagement.service.record;

import com.aslan.academymanagement.domain.*;
import com.aslan.academymanagement.dto.RecordRequest;
import com.aslan.academymanagement.repository.LearningHistoryRepository;
import com.aslan.academymanagement.repository.LectureRecordRepository;
import com.aslan.academymanagement.repository.LectureRepository;
import com.aslan.academymanagement.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LectureRecordServiceImpl implements LectureRecordService {

    private final LectureRecordRepository lectureRecordRepository;
    private final LearningHistoryRepository learningHistoryRepository;
    private final LectureRepository lectureRepository;
    private final StudentRepository studentRepository;

    @Override
    public void createRecord(Member teacher, RecordRequest request) {
        // 1. 강의 및 학생 조회
        Lecture lecture = lectureRepository.findById(request.getLectureId())
                .orElseThrow(() -> new IllegalArgumentException("해당 강의가 없습니다."));
        
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 학생이 없습니다."));

        // 2. 권한 체크 (내 강의인지)
        if (!lecture.getTeacher().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("해당 강의에 대한 권한이 없습니다.");
        }

        // 3. 시간 충돌 감지 로직 (요구사항 3.4)
        if (request.getActualStartTime() != null) {
            LocalTime expectedEndTime = request.getActualStartTime().plusMinutes(lecture.getDefaultDuration());
            
            // TODO: 다음 스케줄 조회 및 충돌 체크 로직 구현 필요
            // 현재는 간단히 로그만 남김
            log.info("예상 종료 시간: {}", expectedEndTime);
            
            if (!request.isForceUpdate()) {
                // 충돌 발생 시 예외 던지기 (409 Conflict)
                // throw new IllegalStateException("시간 충돌이 발생했습니다.");
            }
        }

        // 4. 수업 기록 저장
        LectureRecord record = LectureRecord.builder()
                .lecture(lecture)
                .student(student)
                .date(request.getDate())
                .actualStartTime(request.getActualStartTime())
                // actualEndTime은 계산해서 넣거나 null
                .attendanceStatus(request.getAttendanceStatus())
                .dailyLog(request.getDailyLog())
                .materialInfo(request.getMaterialInfo())
                .build();

        LectureRecord savedRecord = lectureRecordRepository.save(record);

        // 5. 학습 이력(History) 자동 생성 (Side Effect)
        createLearningHistory(savedRecord, request.getMaterialInfo());
    }

    private void createLearningHistory(LectureRecord record, Map<String, Object> materialInfo) {
        if (materialInfo == null || materialInfo.isEmpty()) {
            return;
        }

        String title = (String) materialInfo.getOrDefault("title", "교재 정보 없음");
        String page = (String) materialInfo.getOrDefault("page", "");

        LearningHistory history = LearningHistory.builder()
                .student(record.getStudent())
                .lectureRecord(record)
                .materialName(title)
                .progressText(page)
                .build();

        learningHistoryRepository.save(history);
    }
}
