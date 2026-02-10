package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.enums.MemberStatus;
import com.aslan.academymanagement.domain.enums.Role;
import com.aslan.academymanagement.dto.MemberResponse;
import com.aslan.academymanagement.dto.MemberRoleUpdateRequest;
import com.aslan.academymanagement.repository.AcademyRepository;
import com.aslan.academymanagement.repository.MemberRepository;
import com.aslan.academymanagement.service.admin.AdminService;
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

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin") // 경로 변경: /api/v1/admin
@Tag(name = "Admin", description = "관리자(원장) 전용 API")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService; // AdminService 주입
    private final MemberRepository memberRepository; // 기존 사용하던 레포지토리 (필요 시 AdminService로 이동)
    private final AcademyRepository academyRepository; // 기존 사용하던 레포지토리 (필요 시 AdminService로 이동)

    // getMember 헬퍼 메서드는 AdminService로 이동하는 것이 좋지만, 일단은 유지
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

    @GetMapping("/instructors")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "강사 목록 조회", description = "내 학원의 모든 강사(승인 대기 포함) 목록을 조회합니다.")
    public ResponseEntity<List<MemberResponse>> getInstructors(@AuthenticationPrincipal UserDetails userDetails) {
        Member owner = getMember(userDetails);
        List<MemberResponse> instructors = adminService.getInstructors(owner);
        return ResponseEntity.ok(instructors);
    }

    @PutMapping("/instructors/{memberId}/approve")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "강사 가입 승인", description = "대기 중인 강사의 가입을 승인합니다. (인원 제한 체크)")
    public ResponseEntity<Object> approveInstructor(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long memberId) {
        
        try {
            adminService.approveInstructor(getMember(userDetails), memberId);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // PLAN_LIMIT 처리
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/members/{memberId}/role")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "회원 역할 변경", description = "특정 회원의 역할을 강사(INSTRUCTOR) 또는 실장(MANAGER)으로 변경합니다.")
    public ResponseEntity<MemberResponse> updateMemberRole(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long memberId,
            @Valid @RequestBody MemberRoleUpdateRequest request) {
        
        MemberResponse updatedMember = adminService.updateMemberRole(getMember(userDetails), memberId, request.getRole());
        return ResponseEntity.ok(updatedMember);
    }

    // [Task 4.2] 슈퍼 어드민 기능: 학원 인원 제한 상향 (기존과 동일)
    @PutMapping("/academies/{academyId}/limit")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "학원 인원 제한 상향 (슈퍼 어드민)", description = "특정 학원의 최대 인원 수를 변경합니다.")
    public ResponseEntity<Void> updateAcademyLimit(
            @PathVariable Long academyId,
            @RequestParam Integer maxMembers) {
        
        Academy academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학원이 없습니다."));

        academy.updateMaxMembers(maxMembers);
        academyRepository.save(academy);
        
        log.info("👑 슈퍼 어드민: 학원(ID:{}) 인원 제한을 {}명으로 변경했습니다.", academyId, maxMembers);

        return ResponseEntity.ok().build();
    }
}
