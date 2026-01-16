package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.dto.AcademyRequest;
import com.aslan.academymanagement.dto.AcademyResponse;
import com.aslan.academymanagement.service.academy.AcademyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/academies")
@Tag(name = "Academy", description = "학원 관리 API")
@RequiredArgsConstructor
public class AcademyController {

    private final AcademyService academyService;

    @PostMapping
    @Operation(summary = "학원 생성", description = "새로운 학원을 생성합니다. (원장 가입 시 사용)")
    public ResponseEntity<AcademyResponse> createAcademy(@Valid @RequestBody AcademyRequest request) {
        Academy academy = academyService.createAcademy(request);
        return ResponseEntity.ok(AcademyResponse.from(academy));
    }
}
