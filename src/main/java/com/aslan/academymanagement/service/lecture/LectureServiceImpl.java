package com.aslan.academymanagement.service.lecture;

import com.aslan.academymanagement.annotation.Loggable;
import com.aslan.academymanagement.domain.*;
import com.aslan.academymanagement.domain.enums.Role;
import com.aslan.academymanagement.dto.LectureEventDto;
import com.aslan.academymanagement.dto.LectureRequest;
import com.aslan.academymanagement.dto.LectureResponse;
import com.aslan.academymanagement.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;
    private final StudentRepository studentRepository;
    private final LectureStudentRepository lectureStudentRepository;
    private final ScheduleExceptionRepository scheduleExceptionRepository;
    private final MemberRepository memberRepository; // 강사 조회용

    @Transactional
    @Loggable
    @Override
    public LectureResponse createLecture(Member teacher, LectureRequest req) {
        Lecture lecture = req.toLecture();

        if (req.getStartDate() != null) {
            lecture.setStartDate(req.getStartDate());
        } else {
            lecture.setStartDate(LocalDate.now());
        }

        if (req.getEndDate() != null) {
            lecture.setEndDate(req.getEndDate());
        } else {
            lecture.setEndDate(lecture.getStartDate().plusMonths(3));
        }

        lecture.setTeacher(teacher);

        List<Schedule> schedules = req.toSchedules();
        for (Schedule schedule : schedules) {
            lecture.addSchedule(schedule);
        }
        Lecture saved = lectureRepository.save(lecture);

        if (req.getStudentIds() != null && !req.getStudentIds().isEmpty()) {
            List<Student> students = studentRepository.findAllById(req.getStudentIds());
            for (Student student : students) {
                if (!lectureStudentRepository.existsByLectureAndStudent(saved, student)) {
                    LectureStudent lectureStudent = LectureStudent.builder()
                            .lecture(saved)
                            .student(student)
                            .build();
                    lectureStudentRepository.save(lectureStudent);
                }
            }
        }

        saved.getSchedules().size();

        return LectureResponse.from(saved);
    }

    @Transactional
    @Loggable
    @Override
    public List<LectureResponse> retrieveAllLectures() {
        return lectureRepository.findAll()
                .stream()
                .map(LectureResponse::from)
                .toList();
    }

    @Transactional
    @Loggable
    @Override
    public List<LectureResponse> retrieveMyLectures(Member teacher) {
        return lectureRepository.findAllByTeacher(teacher)
                .stream()
                .map(LectureResponse::from)
                .toList();
    }

    @Transactional
    @Override
    public LectureResponse retrieveLecture(Long lectureId) {
        return lectureRepository.findById(lectureId)
                .map(LectureResponse::from)
                .orElse(null);
    }

    @Transactional
    @Override
    public List<LectureEventDto> getLectureEvents(Member loginUser, LocalDate start, LocalDate end, Long instructorId) {
        if (start == null) start = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        if (end == null) end = start.plusDays(6);

        LocalDate finalStart = start;
        LocalDate finalEnd = end;

        List<Lecture> lectures;

        // 권한별 조회 로직 분기
        if (loginUser.getRole() == Role.ROLE_INSTRUCTOR) {
            // 강사는 본인의 강의만 조회
            lectures = lectureRepository.findAllByTeacher(loginUser);
        } else if (loginUser.getRole() == Role.ROLE_OWNER) {
            // 원장은 선택한 강사 또는 전체 조회
            if (instructorId != null) {
                Member instructor = memberRepository.findById(instructorId)
                        .orElseThrow(() -> new IllegalArgumentException("해당 강사가 없습니다."));
                
                // 해당 강사가 우리 학원 소속인지 체크
                if (!instructor.getAcademy().getId().equals(loginUser.getAcademy().getId())) {
                    throw new IllegalArgumentException("다른 학원의 강사입니다.");
                }
                
                lectures = lectureRepository.findAllByTeacher(instructor);
            } else {
                // 전체 조회 (우리 학원의 모든 강의)
                // 현재 findAll()은 모든 학원의 강의를 가져오므로, 학원별 필터링이 필요함.
                // 하지만 LectureRepository에 findAllByAcademy가 없으므로, 
                // 임시로 findAll() 후 stream filter 적용 (추후 Repository 메서드 추가 권장)
                lectures = lectureRepository.findAll().stream()
                        .filter(l -> l.getAcademy().getId().equals(loginUser.getAcademy().getId()))
                        .collect(Collectors.toList());
            }
        } else {
            // 그 외 권한 (슈퍼 어드민 등)은 일단 빈 리스트
            return List.of();
        }

        // 해당 기간의 예외 사항 조회
        List<ScheduleException> exceptions = scheduleExceptionRepository.findByOriginalDateBetween(finalStart, finalEnd);

        return lectures.stream()
                .flatMap(lecture -> LectureEventDto.from(lecture, finalStart, finalEnd, exceptions).stream())
                .collect(Collectors.toList());
    }
}
