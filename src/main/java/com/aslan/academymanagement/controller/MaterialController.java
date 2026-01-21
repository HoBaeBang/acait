package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.MaterialRequest;
import com.aslan.academymanagement.dto.MaterialResponse;
import com.aslan.academymanagement.repository.MemberRepository;
import com.aslan.academymanagement.service.material.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/materials")
@Tag(name = "Material", description = "교재 관리 API")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final MemberRepository memberRepository;

    @GetMapping("/search")
    @Operation(summary = "교재 검색", description = "교재명으로 검색합니다. (공용 교재 + 내 학원 전용 교재가 함께 검색됩니다)")
    public ResponseEntity<List<MaterialResponse>> searchMaterials(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "검색어 (교재명)", example = "수학")
            @RequestParam String keyword) {

        Member member = getMember(userDetails);
        List<MaterialResponse> materials = materialService.searchMaterials(member, keyword);
        return ResponseEntity.ok(materials);
    }

    @PostMapping
    @Operation(summary = "교재 등록", description = "우리 학원 전용 교재를 등록합니다.")
    public ResponseEntity<MaterialResponse> createMaterial(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MaterialRequest request) {

        Member member = getMember(userDetails);
        MaterialResponse response = materialService.createMaterial(member, request);
        return ResponseEntity.ok(response);
    }

    private Member getMember(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return memberRepository.findByGoogleEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));
    }
}
