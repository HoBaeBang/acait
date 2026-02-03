package com.aslan.academymanagement.service.record;

import com.aslan.academymanagement.domain.*;
import com.aslan.academymanagement.domain.enums.AttendanceStatus;
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

        // 3. 보강 연결 로직 (Task 2.2)
        LectureRecord linkedRecord = null;
        if (request.getAttendanceStatus() == AttendanceStatus.MAKEUP) {
            if (request.getLinkedRecordId() == null) {
                throw new IllegalArgumentException("보강 수업은 원본 결석 기록(linkedRecordId)이 필수입니다.");
            }
            linkedRecord = lectureRecordRepository.findById(request.getLinkedRecordId())
                    .orElseThrow(() -> new IllegalArgumentException("원본 결석 기록을 찾을 수 없습니다."));
            
            // 원본 기록이 '보강 필요(REQ_MAKEUP)' 상태인지 확인
            if (linkedRecord.getAttendanceStatus() != AttendanceStatus.REQ_MAKEUP) {
                throw new IllegalStateException("해당 기록은 보강이 필요한 상태가 아닙니다.");
            }
        }

        // 4. 시간 충돌 감지 로직 (요구사항 3.4)
        if (request.getActualStartTime() != null) {
            LocalTime expectedEndTime = request.getActualStartTime().plusMinutes(lecture.getDefaultDuration());
            log.info("예상 종료 시간: {}", expectedEndTime);
            
            if (!request.isForceUpdate()) {
                // 충돌 발생 시 예외 던지기 (409 Conflict)
                // throw new IllegalStateException("시간 충돌이 발생했습니다.");
            }
        }

        // 5. 수업 기록 저장
        LectureRecord record = LectureRecord.builder()
                .academy(lecture.getAcademy()) // 학원 정보 설정 (필수!)
                .lecture(lecture)
                .student(student)
                .date(request.getDate())
                .actualStartTime(request.getActualStartTime())
                .attendanceStatus(request.getAttendanceStatus())
                .linkedRecord(linkedRecord) // 보강 연결
                .dailyLog(request.getDailyLog())
                .materialInfo(request.getMaterialInfo())
                .build();

        LectureRecord savedRecord = lectureRecordRepository.save(record);

        // 6. 학습 이력(History) 자동 생성 (Side Effect)
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
