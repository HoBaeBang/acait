package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.dto.LectureResponse;
import com.aslan.academymanagement.dto.StudentRequest;
import com.aslan.academymanagement.dto.StudentResponse;
import com.aslan.academymanagement.repository.MemberRepository;
import com.aslan.academymanagement.service.student.StudentManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/students")
@Tag(name = "Student", description = "학생 관리 API")
@RequiredArgsConstructor
public class StudentController {

    private final StudentManagementService studentManagementService;
    private final MemberRepository memberRepository;

    @PostMapping
    @Operation(summary = "학생 등록", description = "신규 학생을 등록합니다. 학번은 자동 생성됩니다.")
    public ResponseEntity<StudentResponse> registerStudent(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StudentRequest request
    ) {
        Member teacher = getMember(userDetails);
        Student student = studentManagementService.registerStudent(teacher, request);
        return ResponseEntity.ok(StudentResponse.from(student));
    }

    @GetMapping
    @Operation(summary = "전체 학생 목록 조회", description = "학원 내 등록된 모든 학생 목록을 조회합니다.")
    public ResponseEntity<List<StudentResponse>> getAllStudents(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Member teacher = getMember(userDetails);
        List<StudentResponse> students = studentManagementService.getAllStudents(teacher);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{studentNumber}")
    @Operation(summary = "학생 상세 조회", description = "학번(studentNumber)으로 학생 정보를 조회합니다.")
    public ResponseEntity<StudentResponse> getStudent(
            @Parameter(description = "학번 (예: 2026A1B2)", required = true)
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

    @DeleteMapping("/{studentNumber}")
    @Operation(summary = "학생 퇴원 처리", description = "학생을 퇴원 상태(DISCHARGED)로 변경합니다. (논리 삭제)")
    public ResponseEntity<Void> dischargeStudent(
            @PathVariable String studentNumber
    ) {
        studentManagementService.dischargeStudent(studentNumber);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/top")
    @Operation(summary = "우수 학생 조회", description = "우수 학생 목록을 조회합니다. (현재는 전체 목록 반환)")
    public ResponseEntity<List<StudentResponse>> getTopStudents() {
        List<Student> students = studentManagementService.getTopStudents();
        List<StudentResponse> responses = students.stream()
                .map(StudentResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{studentNumber}/lectures")
    @Operation(summary = "학생 수강 강의 조회", description = "특정 학생이 수강 중인 강의 목록을 조회합니다.")
    public ResponseEntity<List<LectureResponse>> getEnrolledLectures(
            @PathVariable String studentNumber
    ) {
        List<LectureResponse> lectures = studentManagementService.getEnrolledLectures(studentNumber);
        return ResponseEntity.ok(lectures);
    }

    private Member getMember(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return memberRepository.findByGoogleEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));
    }
}
