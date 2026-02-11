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
    private final MemberRepository memberRepository;

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

        // 강사 배정 로직 수정
        if (req.getInstructorId() != null) {
            // 원장 또는 실장만 다른 강사를 지정할 수 있음
            if (teacher.getRole() != Role.ROLE_OWNER && teacher.getRole() != Role.ROLE_MANAGER) {
                throw new IllegalArgumentException("강사를 지정할 권한이 없습니다.");
            }

            Member instructor = memberRepository.findById(req.getInstructorId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 강사가 없습니다."));

            if (!instructor.getAcademy().getId().equals(teacher.getAcademy().getId())) {
                throw new IllegalArgumentException("다른 학원의 강사입니다.");
            }
            lecture.setTeacher(instructor);
        } else {
            // 기본값: 로그인한 사용자
            lecture.setTeacher(teacher);
        }

        // 학원 정보 설정 (필수)
        lecture.setAcademy(teacher.getAcademy());

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
    public List<LectureEventDto> getLectureEvents(Member loginUser, LocalDate start, LocalDate end, Long instructorId, Boolean viewAll) {
        if (start == null) start = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        if (end == null) end = start.plusDays(6);

        LocalDate finalStart = start;
        LocalDate finalEnd = end;

        List<Lecture> lectures;

        // 1. instructorId가 있으면 해당 강사로 필터링 (원장/실장만 가능)
        if (instructorId != null) {
            if (loginUser.getRole() != Role.ROLE_OWNER && loginUser.getRole() != Role.ROLE_MANAGER) {
                throw new IllegalArgumentException("다른 강사의 일정을 조회할 권한이 없습니다.");
            }
            Member instructor = memberRepository.findById(instructorId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 강사가 없습니다."));
            
            if (!instructor.getAcademy().getId().equals(loginUser.getAcademy().getId())) {
                throw new IllegalArgumentException("다른 학원의 강사입니다.");
            }
            lectures = lectureRepository.findAllByTeacher(instructor);
        }
        // 2. viewAll=true이면 전체 조회 (원장/실장만 가능)
        else if (Boolean.TRUE.equals(viewAll)) {
            if (loginUser.getRole() != Role.ROLE_OWNER && loginUser.getRole() != Role.ROLE_MANAGER) {
                throw new IllegalArgumentException("전체 일정을 조회할 권한이 없습니다.");
            }
            // 학원 전체 강의 조회
            lectures = lectureRepository.findAll().stream()
                    .filter(l -> l.getAcademy().getId().equals(loginUser.getAcademy().getId()))
                    .collect(Collectors.toList());
        }
        // 3. 그 외의 경우 (기본값): 로그인한 사용자(본인)의 강의 조회
        else {
            lectures = lectureRepository.findAllByTeacher(loginUser);
        }

        // 해당 기간의 예외 사항 조회
        List<ScheduleException> exceptions = scheduleExceptionRepository.findByOriginalDateBetween(finalStart, finalEnd);

        return lectures.stream()
                .flatMap(lecture -> LectureEventDto.from(lecture, finalStart, finalEnd, exceptions).stream())
                .collect(Collectors.toList());
    }
}
