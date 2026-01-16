package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.enums.MemberStatus;
import com.aslan.academymanagement.domain.enums.Role;
import com.aslan.academymanagement.dto.MemberResponse;
import com.aslan.academymanagement.repository.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Admin", description = "관리자(원장) 전용 API")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class AdminController {

    private final MemberRepository memberRepository;

    @GetMapping("/instructors")
    @Operation(summary = "강사 목록 조회", description = "내 학원의 모든 강사(승인 대기 포함) 목록을 조회합니다.")
    public ResponseEntity<List<MemberResponse>> getInstructors(@AuthenticationPrincipal UserDetails userDetails) {
        Member owner = getMember(userDetails);
        Academy academy = owner.getAcademy();

        // 내 학원 소속이면서 ROLE_INSTRUCTOR인 회원만 조회
        List<MemberResponse> instructors = memberRepository.findAllByAcademy(academy).stream()
                .filter(member -> member.getRole() == Role.ROLE_INSTRUCTOR)
                .map(MemberResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(instructors);
    }

    @PutMapping("/instructors/{memberId}/approve")
    @Operation(summary = "강사 가입 승인", description = "대기 중인 강사의 가입을 승인합니다. (인원 제한 체크)")
    public ResponseEntity<Object> approveInstructor(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long memberId) {
        
        Member owner = getMember(userDetails);
        Academy academy = owner.getAcademy();

        Member instructor = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));

        // 내 학원 소속인지 확인 (보안)
        if (!instructor.getAcademy().getId().equals(academy.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("다른 학원의 강사를 승인할 수 없습니다.");
        }

        if (instructor.getStatus() != MemberStatus.PENDING) {
            return ResponseEntity.badRequest().body("승인 대기 상태인 회원만 승인할 수 있습니다.");
        }

        // 인원 제한 체크 (현재 ACTIVE 상태인 강사 수 + 1 > maxMembers)
        long activeCount = memberRepository.countByAcademyAndStatus(academy, MemberStatus.ACTIVE);
        if (activeCount >= academy.getMaxMembers()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("PLAN_LIMIT");
        }

        instructor.approve();
        memberRepository.save(instructor);

        return ResponseEntity.ok().build();
    }

    private Member getMember(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        log.info("🔍 AdminController.getMember 호출: username={}", userDetails.getUsername());
        
        return memberRepository.findByGoogleEmail(userDetails.getUsername())
                .orElseThrow(() -> {
                    log.error("❌ 사용자 정보 조회 실패: username={}", userDetails.getUsername());
                    return new IllegalArgumentException("사용자 정보가 없습니다.");
                });
    }
}
