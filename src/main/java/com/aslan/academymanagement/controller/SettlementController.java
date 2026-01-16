package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.SettlementResponse;
import com.aslan.academymanagement.repository.MemberRepository;
import com.aslan.academymanagement.service.settlement.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/settlements")
@Tag(name = "Settlement", description = "정산 관리 API")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;
    private final MemberRepository memberRepository;

    @GetMapping("/dashboard")
    @Operation(summary = "월별 정산 현황 조회 (원장용)", description = "특정 월의 학원 전체 정산 현황을 조회합니다.")
    public ResponseEntity<List<SettlementResponse>> getMonthlySettlements(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String yearMonth) {

        Member admin = getMember(userDetails);
        List<SettlementResponse> settlements = settlementService.getMonthlySettlements(admin, yearMonth);
        return ResponseEntity.ok(settlements);
    }

    private Member getMember(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return memberRepository.findByGoogleEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));
    }
}
