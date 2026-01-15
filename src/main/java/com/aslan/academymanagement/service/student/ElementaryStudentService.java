package com.aslan.academymanagement.service.student;

import com.aslan.academymanagement.annotation.Loggable;
import com.aslan.academymanagement.annotation.Monitored;
import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.domain.enums.Division;
import com.aslan.academymanagement.domain.enums.StudentStatus;
import com.aslan.academymanagement.dto.StudentRequest;
import com.aslan.academymanagement.repository.StudentRepository;
import com.aslan.academymanagement.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@Profile("elementary")
@RequiredArgsConstructor
public class ElementaryStudentService implements StudentManagementService {

    private final StudentRepository studentRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    @Loggable
    @Monitored
    public Student registerStudent(StudentRequest request) {
        log.info("👶 초등부 학생 등록 시작: {}", request.getName());

        if (request.getParentPhone() == null || request.getParentPhone().isEmpty()) {
            throw new IllegalArgumentException("초등부는 학부모 연락처가 필수입니다!");
        }

        if (studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new IllegalArgumentException("이미 존재하는 학생 번호입니다: " + request.getStudentNumber());
        }

        // DTO -> Entity 변환 (toEntity 메서드 활용)
        Student student = request.toEntity();
        
        // 초등부 강제 설정 (혹시 DTO에서 잘못 넘어왔을 경우 대비)
        student.setDivision(Division.ELEMENTARY);

        Student saved = studentRepository.save(student);

        notificationService.notifyParent(
                saved.getParentPhone(),
                String.format("🎉 %s 학생이 초등부에 등록되었습니다!", saved.getName())
        );

        return saved;
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
        
        // 이미 퇴원한 학생인지 확인
        if (student.getStatus() == StudentStatus.DISCHARGED) {
            throw new IllegalStateException("이미 퇴원 처리된 학생입니다.");
        }

        // 상태 변경 및 퇴원일 기록 (오늘 날짜)
        student.setStatus(StudentStatus.DISCHARGED);
        student.setDischargeDate(LocalDate.now());
        
        studentRepository.save(student);
        
        log.info("👋 초등부 학생 퇴원 처리: {} (퇴원일: {})", student.getName(), student.getDischargeDate());
    }

    @Override
    @Transactional(readOnly = true)
    @Monitored
    public List<Student> getTopStudents() {
        // TODO: 우수 학생 기준 변경 필요 (현재는 임시로 전체 조회)
        log.info("👶 초등부 우수 학생 조회");
        return studentRepository.findByDivision(Division.ELEMENTARY);
    }

    @Override
    public String getDivisionType() {
        return "ELEMENTARY (초등부)";
    }
}
