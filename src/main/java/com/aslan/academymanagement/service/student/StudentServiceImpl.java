package com.aslan.academymanagement.service.student;

import com.aslan.academymanagement.annotation.Loggable;
import com.aslan.academymanagement.annotation.Monitored;
import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.domain.Lecture;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.domain.enums.StudentStatus;
import com.aslan.academymanagement.dto.LectureResponse;
import com.aslan.academymanagement.dto.StudentRequest;
import com.aslan.academymanagement.dto.StudentResponse;
import com.aslan.academymanagement.repository.LectureRepository;
import com.aslan.academymanagement.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentManagementService {

    private final StudentRepository studentRepository;
    private final LectureRepository lectureRepository;

    @Override
    @Transactional
    @Loggable
    @Monitored
    public Student registerStudent(Member teacher, StudentRequest request) {
        log.info("🎓 학생 등록 시작: {}", request.getName());

        Academy academy = teacher.getAcademy();

        String year = String.valueOf(LocalDate.now().getYear());
        String uniqueId = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String studentNumber = year + uniqueId;

        while (studentRepository.existsByStudentNumber(studentNumber)) {
            uniqueId = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            studentNumber = year + uniqueId;
        }

        Student student = Student.builder()
                .academy(academy)
                .studentNumber(studentNumber)
                .name(request.getName())
                .school(request.getSchool())
                .grade(request.getGrade())
                .birthDate(request.getBirthDate())
                .parentPhone(request.getParentPhone())
                .parentEmail(request.getParentEmail())
                .memo(request.getMemo())
                .build();

        return studentRepository.save(student);
    }

    @Override
    @Transactional(readOnly = true)
    public Student getStudent(String studentNumber) {
        return studentRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + studentNumber));
    }

    @Override
    @Transactional
    @Loggable
    public Student updateStudent(String studentNumber, StudentRequest request) {
        Student student = getStudent(studentNumber);

        student.setName(request.getName());
        student.setSchool(request.getSchool());
        student.setGrade(request.getGrade());
        student.setBirthDate(request.getBirthDate());
        student.setParentPhone(request.getParentPhone());
        student.setParentEmail(request.getParentEmail());
        student.setMemo(request.getMemo());

        return studentRepository.save(student);
    }

    @Override
    @Transactional
    @Loggable
    public void dischargeStudent(String studentNumber) {
        Student student = getStudent(studentNumber);

        if (student.getStatus() == StudentStatus.DISCHARGED) {
            throw new IllegalStateException("이미 퇴원 처리된 학생입니다.");
        }

        student.setStatus(StudentStatus.DISCHARGED);
        student.setDischargeDate(LocalDate.now());

        studentRepository.save(student);
        log.info("👋 학생 퇴원 처리: {} (퇴원일: {})", student.getName(), student.getDischargeDate());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> getTopStudents() {
        return studentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureResponse> getEnrolledLectures(String studentNumber) {
        Student student = getStudent(studentNumber);
        List<Lecture> lectures = lectureRepository.findAllByStudentId(student.getId());
        return lectures.stream()
                .map(LectureResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents(Member teacher) {
        Academy academy = teacher.getAcademy();
        return studentRepository.findAllByAcademy(academy).stream()
                .map(StudentResponse::from)
                .collect(Collectors.toList());
    }
}
