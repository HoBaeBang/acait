package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.ScheduleUpdateRequest;
import com.aslan.academymanagement.repository.MemberRepository;
import com.aslan.academymanagement.service.schedule.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/schedules")
@Tag(name = "Schedule", description = "시간표 관리 API")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final MemberRepository memberRepository;

    @PutMapping("/{scheduleId}")
    @Operation(summary = "시간표 수정", description = "반복 일정(마스터 스케줄)을 수정합니다. (이번 주만 vs 앞으로 쭉)")
    public ResponseEntity<Void> updateSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleUpdateRequest request) {

        Member teacher = getMember(userDetails);
        scheduleService.updateSchedule(teacher, scheduleId, request);
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
