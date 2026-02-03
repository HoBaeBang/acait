package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.enums.Role;
import com.aslan.academymanagement.dto.SettlementDetailResponse;
import com.aslan.academymanagement.dto.SettlementForecastResponse;
import com.aslan.academymanagement.dto.SettlementResponse;
import com.aslan.academymanagement.repository.MemberRepository;
import com.aslan.academymanagement.service.settlement.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/v1/settlements")
@Tag(name = "Settlement", description = "정산 및 세금 관리 API")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;
    private final MemberRepository memberRepository;

    @PostMapping("/calculate")
    @Operation(summary = "월별 정산 실행", description = "특정 월의 정산을 실행합니다. (원장: 전체, 강사: 본인)")
    public ResponseEntity<String> calculateSettlement(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "정산 월 (YYYY-MM)", example = "2026-01")
            @RequestParam String yearMonth) {

        Member member = getMember(userDetails);

        if (member.getRole() == Role.ROLE_OWNER) {
            settlementService.calculateMonthlySettlement(yearMonth);
            return ResponseEntity.ok(yearMonth + " 전체 정산이 완료되었습니다.");
        } else if (member.getRole() == Role.ROLE_INSTRUCTOR) {
            settlementService.calculateMySettlement(member, yearMonth);
            return ResponseEntity.ok(yearMonth + " 정산이 완료되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("정산 실행 권한이 없습니다.");
        }
    }

    @GetMapping("/dashboard")
    @Operation(summary = "월별 정산 현황 조회 (원장용)", description = "특정 월의 학원 전체 정산 현황(강사별 요약)을 조회합니다.")
    public ResponseEntity<List<SettlementResponse>> getMonthlySettlements(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "조회 월 (YYYY-MM)", example = "2026-01")
            @RequestParam String yearMonth) {

        Member admin = getMember(userDetails);
        List<SettlementResponse> settlements = settlementService.getMonthlySettlements(admin, yearMonth);
        return ResponseEntity.ok(settlements);
    }

    @GetMapping("/my")
    @Operation(summary = "내 정산 내역 조회 (강사용)", description = "로그인한 강사의 월별 정산 내역을 조회합니다.")
    public ResponseEntity<List<SettlementResponse>> getMySettlements(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "조회 월 (YYYY-MM)", example = "2026-01")
            @RequestParam String yearMonth) {

        Member instructor = getMember(userDetails);
        List<SettlementResponse> settlements = settlementService.getMySettlements(instructor, yearMonth);
        return ResponseEntity.ok(settlements);
    }

    @GetMapping("/forecast")
    @Operation(summary = "예상 정산 금액 조회 (강사용)", description = "이번 달의 확정 금액과 남은 수업에 대한 예상 금액을 조회합니다.")
    public ResponseEntity<SettlementForecastResponse> getSettlementForecast(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "조회 월 (YYYY-MM)", example = "2026-01")
            @RequestParam String yearMonth) {

        Member instructor = getMember(userDetails);
        SettlementForecastResponse forecast = settlementService.getSettlementForecast(instructor, yearMonth);
        return ResponseEntity.ok(forecast);
    }

    @GetMapping("/{settlementId}/details")
    @Operation(summary = "정산 상세 내역 조회", description = "특정 정산 건에 포함된 상세 수업 기록 목록을 조회합니다.")
    public ResponseEntity<List<SettlementDetailResponse>> getSettlementDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long settlementId) {

        Member member = getMember(userDetails);
        List<SettlementDetailResponse> details = settlementService.getSettlementDetails(member, settlementId);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/excel")
    @Operation(summary = "정산 내역 엑셀 다운로드", description = "특정 월의 정산 내역을 엑셀 파일(.xlsx)로 다운로드합니다.")
    public ResponseEntity<InputStreamResource> downloadExcel(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "다운로드 월 (YYYY-MM)", example = "2026-01")
            @RequestParam String yearMonth) {

        Member admin = getMember(userDetails);
        ByteArrayInputStream in = settlementService.exportSettlementToExcel(admin, yearMonth);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=settlement_" + yearMonth + ".xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    private Member getMember(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return memberRepository.findByGoogleEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));
    }
}
