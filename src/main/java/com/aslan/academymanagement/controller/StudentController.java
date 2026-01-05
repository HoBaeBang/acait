package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.dto.StudentRequest;
import com.aslan.academymanagement.dto.StudentResponse;
import com.aslan.academymanagement.service.student.StudentManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 학생 관리 REST API 컨트롤러
 *
 * ========================================
 * Spring DI 실습 - 인터페이스 기반 주입
 * ========================================
 *
 * @RestController:
 * - @Controller + @ResponseBody의 조합
 * - 모든 메서드의 반환값이 HTTP Response Body에 직접 작성됨
 * - JSON 형태로 자동 변환 (Jackson 라이브러리 사용)
 *
 * @RequiredArgsConstructor:
 * - final 필드인 studentManagementService에 대한 생성자를 자동 생성
 * - Spring이 적절한 구현체를 자동으로 주입
 *
 * 의존성 주입 동작 방식:
 * 1. Spring이 StudentController Bean을 생성할 때
 * 2. 활성화된 Profile을 확인 (elementary 또는 middle)
 * 3. 해당 Profile의 StudentManagementService 구현체를 찾음
 *    - elementary → ElementaryStudentService
 *    - middle → MiddleStudentService
 * 4. 찾은 구현체를 생성자를 통해 주입
 *
 * 이것이 DI의 핵심입니다!
 * Controller는 구체적인 구현체(ElementaryStudentService, MiddleStudentService)를
 * 알 필요가 없고, 인터페이스(StudentManagementService)만 의존합니다.
 * 실제 어떤 구현체가 주입될지는 Profile 설정에 따라 Spring이 결정합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    // 인터페이스 타입으로 선언 - DI의 핵심!
    // 실제 주입되는 객체는 활성화된 Profile에 따라 달라집니다.
    private final StudentManagementService studentManagementService;

    @PostMapping
    public ResponseEntity<StudentResponse> registerStudent(
            @Valid @RequestBody StudentRequest request
    ) {
        Student student = studentManagementService.registerStudent(request);
        return ResponseEntity.ok(StudentResponse.from(student));
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<StudentResponse> getStudent(
            @PathVariable String studentId
    ) {
        Student student = studentManagementService.getStudent(studentId);
        return ResponseEntity.ok(StudentResponse.from(student));
    }

    @PutMapping("/{studentId}")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable String studentId,
            @Valid @RequestBody StudentRequest request
    ) {
        Student student = studentManagementService.updateStudent(studentId, request);
        return ResponseEntity.ok(StudentResponse.from(student));
    }

    @PostMapping("/{studentId}/attendance")
    public ResponseEntity<String> checkAttendance(
            @PathVariable String studentId
    ) {
        studentManagementService.checkAttendance(studentId);
        return ResponseEntity.ok("출석 체크 완료");
    }

    @PostMapping("/{studentId}/score")
    public ResponseEntity<String> updateScore(
            @PathVariable String studentId,
            @RequestParam Double score
    ) {
        studentManagementService.updateScore(studentId, score);
        return ResponseEntity.ok("성적 입력 완료");
    }

    @GetMapping("/top")
    public ResponseEntity<List<StudentResponse>> getTopStudents() {
        List<Student> students = studentManagementService.getTopStudents();
        List<StudentResponse> responses = students.stream()
                .map(StudentResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/division")
    public ResponseEntity<String> getDivisionType() {
        return ResponseEntity.ok(
                "현재 부서: " + studentManagementService.getDivisionType()
        );
    }
}
