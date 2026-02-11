package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.StudentResponse;
import com.aslan.academymanagement.repository.MemberRepository;
import com.aslan.academymanagement.service.lecture.LectureStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lecture")
@Tag(name = "Lecture Student", description = "강의 수강생 관리 API")
@RequiredArgsConstructor
public class LectureStudentController {

    private final LectureStudentService lectureStudentService;
    private final MemberRepository memberRepository;

    @PostMapping("/{lectureId}/students/{studentNumber}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'INSTRUCTOR')")
    @Operation(summary = "강의에 학생 등록", description = "특정 강의에 학생을 등록합니다. (학번 사용)")
    public ResponseEntity<Void> registerStudent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long lectureId,
            @Parameter(description = "학번 (예: 20263FE2)")
            @PathVariable String studentNumber) {

        Member teacher = getMember(userDetails);
        lectureStudentService.registerStudent(teacher, lectureId, studentNumber);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{lectureId}/students/{studentNumber}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'INSTRUCTOR')")
    @Operation(summary = "강의에서 학생 제외", description = "특정 강의에서 학생 등록을 취소합니다. (학번 사용)")
    public ResponseEntity<Void> removeStudent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long lectureId,
            @Parameter(description = "학번 (예: 20263FE2)")
            @PathVariable String studentNumber) {

        Member teacher = getMember(userDetails);
        lectureStudentService.removeStudent(teacher, lectureId, studentNumber);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{lectureId}/students")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'INSTRUCTOR')")
    @Operation(summary = "강의 수강생 목록 조회", description = "특정 강의를 수강하는 학생 목록을 조회합니다.")
    public ResponseEntity<List<StudentResponse>> getStudentsByLecture(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long lectureId) {

        Member teacher = getMember(userDetails);
        List<StudentResponse> students = lectureStudentService.getStudentsByLecture(teacher, lectureId);
        return ResponseEntity.ok(students);
    }

    private Member getMember(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return memberRepository.findByGoogleEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));
    }
}
