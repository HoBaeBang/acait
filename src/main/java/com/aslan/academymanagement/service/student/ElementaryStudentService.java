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

/**
 * 초등부 학생 관리 서비스 구현체
 *
 * ========================================
 * Spring DI (Dependency Injection) 실습
 * ========================================
 *
 * @Service: 이 클래스를 Spring Bean으로 등록하고, 서비스 계층임을 명시
 *
 * @Profile("elementary"):
 * - application.yml에서 spring.profiles.active=elementary 일 때만 이 Bean이 활성화됨
 * - 환경별로 다른 구현체를 선택할 수 있는 강력한 기능
 * - 개발/테스트/운영 환경 분리, 기능별 분리 등에 활용
 *
 * @RequiredArgsConstructor (Lombok):
 * - final 필드에 대한 생성자를 자동으로 생성
 * - Spring 4.3+에서는 생성자가 하나만 있으면 @Autowired 생략 가능
 * - 생성자 주입(Constructor Injection) 방식 사용
 *
 * DI의 세 가지 방식:
 * 1. 생성자 주입 (권장) ✅
 *    - 불변성 보장 (final 사용 가능)
 *    - 순환 참조 방지
 *    - 테스트 용이
 * 2. Setter 주입
 *    - 선택적 의존성에 사용
 * 3. 필드 주입 (@Autowired를 필드에 직접)
 *    - 간결하지만 테스트가 어려움 (권장하지 않음)
 *
 * 주입되는 의존성:
 * - StudentRepository: JPA Repository, 데이터베이스 접근
 * - NotificationService: 알림 서비스, 학부모/학생에게 메시지 전송
 */
@Slf4j
@Service
@Profile("elementary")
@RequiredArgsConstructor
public class ElementaryStudentService implements StudentManagementService {

    // final 키워드로 불변성 보장 - 생성자 주입의 장점
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    @Loggable
    @Monitored
    public Student registerStudent(StudentRequest request) {
        log.info("👶 초등부 학생 등록 시작: {}", request.getName());

        if (request.getParentPhoneNumber() == null ||
                request.getParentPhoneNumber().isEmpty()) {
            throw new IllegalArgumentException("초등부는 학부모 연락처가 필수입니다!");
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
                .division(Division.ELEMENTARY)
                .attendanceCount(0)
                .averageScore(0.0)
                .specialNotes("🌟 신규 등록 - 귀가 동의서 필요")
                .build();

        Student saved = studentRepository.save(student);

        notificationService.notifyParent(
                saved.getParentPhoneNumber(),
                String.format("🎉 %s 학생이 초등부에 등록되었습니다!", saved.getName())
        );

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

        log.info("👶 초등부 출석 체크: {} (총 {}일)", student.getName(), student.getAttendanceCount());

        notificationService.notifyParent(
                student.getParentPhoneNumber(),
                String.format("✅ %s 학생이 출석했습니다! (총 %d일)",
                        student.getName(), student.getAttendanceCount())
        );

        if (student.getAttendanceCount() == 100) {
            notificationService.notifyParent(
                    student.getParentPhoneNumber(),
                    String.format("🎊 축하합니다! %s 학생이 100일 개근을 달성했습니다!", student.getName())
            );
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

        log.info("👶 초등부 성적 입력: {} - {}점 (평균: {})",
                student.getName(), score, student.getAverageScore());

        if (score >= 90) {
            notificationService.notifyParent(
                    student.getParentPhoneNumber(),
                    String.format("⭐ %s 학생이 %.0f점을 받았어요! 칭찬 스티커 1개 획득!",
                            student.getName(), score)
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Monitored
    public List<Student> getTopStudents() {
        log.info("👶 초등부 우수 학생 조회 (출석 80일 이상)");

        List<Student> allStudents = studentRepository.findByDivision(Division.ELEMENTARY);
        return allStudents.stream()
                .filter(s -> s.getAttendanceCount() != null && s.getAttendanceCount() >= 80)
                .toList();
    }

    @Override
    public String getDivisionType() {
        return "ELEMENTARY (초등부)";
    }
}
