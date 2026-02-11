package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.StudentBalanceResponse;
import com.aslan.academymanagement.dto.TuitionPaymentRequest;
import com.aslan.academymanagement.repository.MemberRepository;
import com.aslan.academymanagement.service.tuition.TuitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/students")
@Tag(name = "Tuition", description = "수강료 및 잔액 관리 API")
@RequiredArgsConstructor
public class TuitionPaymentController {

    private final TuitionService tuitionService;
    private final MemberRepository memberRepository;

    private Member getMember(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return memberRepository.findByGoogleEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));
    }

    @PostMapping("/{studentId}/payments")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    @Operation(summary = "수강료 납부", description = "학생의 수강료 납부 내역을 등록합니다.")
    public ResponseEntity<Void> createPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studentId,
            @Valid @RequestBody TuitionPaymentRequest request) {

        Member manager = getMember(userDetails);
        tuitionService.createPayment(manager, studentId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{studentId}/balance")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    @Operation(summary = "월별 잔액 조회", description = "학생의 특정 월 잔액 및 이월금 내역을 조회합니다.")
    public ResponseEntity<StudentBalanceResponse> getBalance(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studentId,
            @RequestParam String yearMonth) {

        Member manager = getMember(userDetails);
        StudentBalanceResponse response = tuitionService.getBalance(manager, studentId, yearMonth);
        return ResponseEntity.ok(response);
    }
}
