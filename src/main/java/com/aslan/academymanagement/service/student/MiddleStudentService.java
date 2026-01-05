package com.aslan.academymanagement.service.student;


import com.aslan.academymanagement.annotation.AttendanceRequired;
import com.aslan.academymanagement.annotation.Loggable;
import com.aslan.academymanagement.annotation.Monitored;
import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.domain.enums.Division;
import com.aslan.academymanagement.dto.StudentRequest;
import com.aslan.academymanagement.repository.StudentRepository;
import com.aslan.academymanagement.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Profile("middle")
@RequiredArgsConstructor
public class MiddleStudentService implements StudentManagementService {

    private final StudentRepository studentRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    @Loggable
    @Monitored
    public Student registerStudent(StudentRequest request) {
        log.info("🎓 중등부 학생 등록 시작: {}", request.getName());

        if (request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty()) {
            throw new IllegalArgumentException("중등부는 본인 연락처가 필수입니다!");
        }

        if (studentRepository.existsByStudentId(request.getStudentId())) {
            throw new IllegalArgumentException("이미 존재하는 학생 번호입니다: " + request.getStudentId());
        }

        Student student = Student.builder()
                .studentId(request.getStudentId())
                .name(request.getName())
                .birthDate(request.getBirthDate())
                .phoneNumber(request.getPhoneNumber())
                .parentPhoneNumber(request.getParentPhoneNumber())
                .grade(request.getGrade())
                .division(Division.MIDDLE)
                .attendanceCount(0)
                .averageScore(0.0)
                .specialNotes("📚 신규 등록 - 레벨 테스트 예정")
                .build();

        Student saved = studentRepository.save(student);

        notificationService.notifyStudent(
                saved.getPhoneNumber(),
                String.format("🎉 %s님, 중등부에 등록되었습니다! 열심히 공부해요!", saved.getName())
        );

        if (saved.getParentPhoneNumber() != null) {
            notificationService.notifyParent(
                    saved.getParentPhoneNumber(),
                    String.format("%s 학생이 중등부에 등록되었습니다.", saved.getName())
            );
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Student getStudent(String studentId) {
        return studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + studentId));
    }

    @Override
    @Transactional
    @Loggable
    public Student updateStudent(String studentId, StudentRequest request) {
        Student student = getStudent(studentId);

        student.setName(request.getName());
        student.setPhoneNumber(request.getPhoneNumber());
        student.setParentPhoneNumber(request.getParentPhoneNumber());
        student.setGrade(request.getGrade());
        student.setSpecialNotes(request.getSpecialNotes());

        return studentRepository.save(student);
    }

    @Override
    @Transactional
    @AttendanceRequired
    public void checkAttendance(String studentId) {
        Student student = getStudent(studentId);

        student.setAttendanceCount(student.getAttendanceCount() + 1);
        studentRepository.save(student);

        log.info("🎓 중등부 출석 체크: {} (총 {}일)", student.getName(), student.getAttendanceCount());

        notificationService.notifyStudent(
                student.getPhoneNumber(),
                String.format("✅ 출석 체크 완료! (총 %d일)", student.getAttendanceCount())
        );

        if (student.getAttendanceCount() >= 50) {
            double attendanceRate = (student.getAttendanceCount() / 50.0) * 100;
            if (attendanceRate < 90) {
                notificationService.notifyStudent(
                        student.getPhoneNumber(),
                        String.format("⚠️ 출석률이 %.1f%%입니다. 출석 관리에 주의하세요!", attendanceRate)
                );
            }
        }
    }

    @Override
    @Transactional
    @Loggable
    public void updateScore(String studentId, Double score) {
        Student student = getStudent(studentId);

        double currentAvg = student.getAverageScore() != null ? student.getAverageScore() : 0.0;
        int count = student.getAttendanceCount() != null ? student.getAttendanceCount() : 1;

        double newAvg = ((currentAvg * count) + score) / (count + 1);
        student.setAverageScore(Math.round(newAvg * 100.0) / 100.0);

        studentRepository.save(student);

        String grade = calculateGrade(student.getAverageScore());

        log.info("🎓 중등부 성적 입력: {} - {}점 (평균: {}, 등급: {})",
                student.getName(), score, student.getAverageScore(), grade);

        notificationService.notifyStudent(
                student.getPhoneNumber(),
                String.format("📊 성적이 입력되었습니다! 점수: %.0f점, 평균: %.1f점, 등급: %s",
                        score, student.getAverageScore(), grade)
        );

        if (student.getAverageScore() >= 90 && student.getParentPhoneNumber() != null) {
            notificationService.notifyParent(
                    student.getParentPhoneNumber(),
                    String.format("🏆 %s 학생의 평균이 %.1f점입니다! (등급: %s)",
                            student.getName(), student.getAverageScore(), grade)
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Monitored
    public List<Student> getTopStudents() {
        log.info("🎓 중등부 우수 학생 조회 (평균 90점 이상)");

        return studentRepository.findHighAchievers(Division.MIDDLE, 90.0);
    }

    @Override
    public String getDivisionType() {
        return "MIDDLE (중등부)";
    }

    private String calculateGrade(Double averageScore) {
        if (averageScore >= 90) return "A";
        if (averageScore >= 80) return "B";
        if (averageScore >= 70) return "C";
        if (averageScore >= 60) return "D";
        return "F";
    }
}
