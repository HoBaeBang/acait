package com.aslan.academymanagement.service.student;

import com.aslan.academymanagement.annotation.Loggable;
import com.aslan.academymanagement.annotation.Monitored;
import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.domain.enums.StudentStatus;
import com.aslan.academymanagement.dto.StudentRequest;
import com.aslan.academymanagement.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentManagementService {

    private final StudentRepository studentRepository;

    @Override
    @Transactional
    @Loggable
    @Monitored
    public Student registerStudent(Member teacher, StudentRequest request) {
        log.info("🎓 학생 등록 시작: {}", request.getName());

        Academy academy = teacher.getAcademy();

        // 학번 자동 생성 로직 (YYYY + UUID 4자리)
        // 실제로는 DB 시퀀스나 Redis를 써서 순차 증가(0001, 0002)를 구현해야 하지만,
        // 여기서는 간단하게 UUID 앞 4자리를 사용하여 유니크성을 확보함.
        // (동시성 문제가 발생할 수 있으므로 실무에서는 AtomicLong이나 DB Sequence 사용 권장)
        String year = String.valueOf(LocalDate.now().getYear());
        String uniqueId = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String studentNumber = year + uniqueId;

        // 중복 체크 (혹시 모를 충돌 대비)
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
        // TODO: 우수 학생 기준 재정의 필요
        return studentRepository.findAll();
    }
}
