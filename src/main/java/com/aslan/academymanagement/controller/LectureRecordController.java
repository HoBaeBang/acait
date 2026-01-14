package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.RecordRequest;
import com.aslan.academymanagement.repository.MemberRepository;
import com.aslan.academymanagement.service.record.LectureRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/records")
@Tag(name = "Lecture Record", description = "수업 기록 및 LMS API")
@RequiredArgsConstructor
public class LectureRecordController {

    private final LectureRecordService lectureRecordService;
    private final MemberRepository memberRepository; // MemberService로 이동 권장

    @PostMapping
    @Operation(summary = "수업 기록 저장", description = "강사가 수업 일지 및 출결 정보를 저장합니다.")
    public ResponseEntity<Void> createRecord(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RecordRequest request) {

        Member teacher = getMember(userDetails);
        lectureRecordService.createRecord(teacher, request);
        return ResponseEntity.ok().build();
    }

    private Member getMember(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return memberRepository.findByGoogleEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));
    }
}
