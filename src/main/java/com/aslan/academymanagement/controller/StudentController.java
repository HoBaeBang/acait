package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.dto.StudentRequest;
import com.aslan.academymanagement.dto.StudentResponse;
import com.aslan.academymanagement.service.student.StudentManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/students")
@Tag(name = "Student", description = "학생 관리 API")
@RequiredArgsConstructor
public class StudentController {

    private final StudentManagementService studentManagementService;

    @PostMapping
    @Operation(summary = "학생 등록", description = "신규 학생을 등록합니다.")
    public ResponseEntity<StudentResponse> registerStudent(
            @Valid @RequestBody StudentRequest request
    ) {
        Student student = studentManagementService.registerStudent(request);
        return ResponseEntity.ok(StudentResponse.from(student));
    }

    @GetMapping("/{studentNumber}")
    @Operation(summary = "학생 조회", description = "학번으로 학생 정보를 조회합니다.")
    public ResponseEntity<StudentResponse> getStudent(
            @PathVariable String studentNumber
    ) {
        Student student = studentManagementService.getStudent(studentNumber);
        return ResponseEntity.ok(StudentResponse.from(student));
    }

    @PutMapping("/{studentNumber}")
    @Operation(summary = "학생 정보 수정", description = "학생 정보를 수정합니다.")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable String studentNumber,
            @Valid @RequestBody StudentRequest request
    ) {
        Student student = studentManagementService.updateStudent(studentNumber, request);
        return ResponseEntity.ok(StudentResponse.from(student));
    }

    @GetMapping("/top")
    @Operation(summary = "우수 학생 조회", description = "우수 학생 목록을 조회합니다.")
    public ResponseEntity<List<StudentResponse>> getTopStudents() {
        List<Student> students = studentManagementService.getTopStudents();
        List<StudentResponse> responses = students.stream()
                .map(StudentResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/division")
    @Operation(summary = "현재 부서 확인", description = "현재 활성화된 부서(Profile)를 확인합니다.")
    public ResponseEntity<String> getDivisionType() {
        return ResponseEntity.ok(
                "현재 부서: " + studentManagementService.getDivisionType()
        );
    }
}
