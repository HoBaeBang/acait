package com.aslan.academymanagement.service.lecture;

import com.aslan.academymanagement.domain.Lecture;
import com.aslan.academymanagement.domain.LectureStudent;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.dto.StudentResponse;
import com.aslan.academymanagement.repository.LectureRepository;
import com.aslan.academymanagement.repository.LectureStudentRepository;
import com.aslan.academymanagement.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LectureStudentServiceImpl implements LectureStudentService {

    private final LectureRepository lectureRepository;
    private final StudentRepository studentRepository;
    private final LectureStudentRepository lectureStudentRepository;

    @Override
    public void registerStudent(Member teacher, Long lectureId, Long studentId) {
        // 1. 강의 조회 및 권한 확인
        Lecture lecture = getLectureWithAuth(teacher, lectureId);

        // 2. 학생 조회
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학생이 없습니다. id=" + studentId));

        // 3. 중복 등록 확인
        if (lectureStudentRepository.existsByLectureAndStudent(lecture, student)) {
            throw new IllegalStateException("이미 등록된 학생입니다.");
        }

        // 4. 등록 (중간 엔티티 생성)
        LectureStudent lectureStudent = LectureStudent.builder()
                .lecture(lecture)
                .student(student)
                .build();

        lectureStudentRepository.save(lectureStudent);
    }

    @Override
    public void removeStudent(Member teacher, Long lectureId, Long studentId) {
        // 1. 강의 조회 및 권한 확인
        Lecture lecture = getLectureWithAuth(teacher, lectureId);

        // 2. 학생 조회
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학생이 없습니다. id=" + studentId));

        // 3. 등록 정보 조회
        LectureStudent lectureStudent = lectureStudentRepository.findByLectureAndStudent(lecture, student)
                .orElseThrow(() -> new IllegalArgumentException("해당 강의에 등록되지 않은 학생입니다."));

        // 4. 삭제
        lectureStudentRepository.delete(lectureStudent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByLecture(Member teacher, Long lectureId) {
        // 1. 강의 조회 및 권한 확인
        Lecture lecture = getLectureWithAuth(teacher, lectureId);

        // 2. 수강생 목록 조회 (중간 엔티티 -> 학생 엔티티 -> DTO 변환)
        return lectureStudentRepository.findAllByLecture(lecture).stream()
                .map(ls -> StudentResponse.from(ls.getStudent()))
                .collect(Collectors.toList());
    }

    // 공통 로직: 강의 조회 및 강사 권한 확인
    private Lecture getLectureWithAuth(Member teacher, Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("해당 강의가 없습니다. id=" + lectureId));

        // 강사 본인의 강의인지 확인 (관리자는 패스하는 로직 추가 가능)
        if (!lecture.getTeacher().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("해당 강의에 대한 권한이 없습니다.");
        }

        return lecture;
    }
}
