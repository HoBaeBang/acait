package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.AcademyRequest;
import com.aslan.academymanagement.dto.AcademyResponse;
import com.aslan.academymanagement.repository.MemberRepository;
import com.aslan.academymanagement.service.academy.AcademyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/academies")
@Tag(name = "Academy", description = "학원 관리 API")
@RequiredArgsConstructor
public class AcademyController {

    private final AcademyService academyService;
    private final MemberRepository memberRepository;

    @PostMapping
    @Operation(summary = "학원 생성", description = "새로운 학원을 생성합니다. (원장 가입 시 사용)")
    public ResponseEntity<AcademyResponse> createAcademy(@Valid @RequestBody AcademyRequest request) {
        Academy academy = academyService.createAcademy(request);
        return ResponseEntity.ok(AcademyResponse.from(academy));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "내 학원 정보 조회", description = "로그인한 원장의 학원 정보를 조회합니다. (초대 코드 포함)")
    public ResponseEntity<AcademyResponse> getMyAcademy(@AuthenticationPrincipal UserDetails userDetails) {
        Member owner = memberRepository.findByGoogleEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));

        return ResponseEntity.ok(AcademyResponse.from(owner.getAcademy()));
    }
}
