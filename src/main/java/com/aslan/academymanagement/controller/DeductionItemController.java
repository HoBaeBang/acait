package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.DeductionItemRequest;
import com.aslan.academymanagement.dto.DeductionItemResponse;
import com.aslan.academymanagement.repository.MemberRepository;
import com.aslan.academymanagement.service.deduction.DeductionItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/academies/deductions")
@Tag(name = "DeductionItem", description = "공제 항목 관리 API")
@RequiredArgsConstructor
public class DeductionItemController {

    private final DeductionItemService deductionItemService;
    private final MemberRepository memberRepository;

    private Member getMember(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return memberRepository.findByGoogleEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "공제 항목 목록 조회", description = "학원에 설정된 공제 항목 목록을 조회합니다.")
    public ResponseEntity<List<DeductionItemResponse>> getDeductionItems(@AuthenticationPrincipal UserDetails userDetails) {
        Member owner = getMember(userDetails);
        return ResponseEntity.ok(deductionItemService.getDeductionItems(owner));
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "공제 항목 추가", description = "새로운 공제 항목을 추가합니다.")
    public ResponseEntity<DeductionItemResponse> createDeductionItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DeductionItemRequest request) {
        Member owner = getMember(userDetails);
        return ResponseEntity.ok(deductionItemService.createDeductionItem(owner, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "공제 항목 수정", description = "기존 공제 항목을 수정합니다.")
    public ResponseEntity<DeductionItemResponse> updateDeductionItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody DeductionItemRequest request) {
        Member owner = getMember(userDetails);
        return ResponseEntity.ok(deductionItemService.updateDeductionItem(owner, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "공제 항목 삭제", description = "공제 항목을 삭제합니다.")
    public ResponseEntity<Void> deleteDeductionItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Member owner = getMember(userDetails);
        deductionItemService.deleteDeductionItem(owner, id);
        return ResponseEntity.ok().build();
    }
}
