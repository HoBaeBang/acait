package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.enums.MemberStatus;
import com.aslan.academymanagement.domain.enums.Role;
import com.aslan.academymanagement.dto.MemberResponse;
import com.aslan.academymanagement.repository.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "관리자(원장) 전용 API")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')") // ADMIN -> OWNER 변경
public class AdminController {

    private final MemberRepository memberRepository;

    @GetMapping("/instructors")
    @Operation(summary = "강사 목록 조회", description = "모든 강사(승인 대기 포함) 목록을 조회합니다.")
    public ResponseEntity<List<MemberResponse>> getInstructors() {
        // ROLE_INSTRUCTOR인 회원만 조회 (PENDING, ACTIVE, REJECTED 모두 포함)
        // 실제로는 QueryDSL이나 별도 메서드로 필터링하는 것이 좋음. 여기서는 전체 조회 후 필터링.
        List<MemberResponse> instructors = memberRepository.findAll().stream()
                .filter(member -> member.getRole() == Role.ROLE_INSTRUCTOR)
                .map(MemberResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(instructors);
    }

    @PutMapping("/instructors/{memberId}/approve")
    @Operation(summary = "강사 가입 승인", description = "대기 중인 강사의 가입을 승인합니다.")
    public ResponseEntity<Void> approveInstructor(@PathVariable Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));

        if (member.getStatus() != MemberStatus.PENDING) {
            throw new IllegalStateException("승인 대기 상태인 회원만 승인할 수 있습니다.");
        }

        member.approve(); // 상태를 ACTIVE로 변경하고 승인 일시 기록
        memberRepository.save(member);

        return ResponseEntity.ok().build();
    }
}
