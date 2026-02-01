package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.LectureEventDto;
import com.aslan.academymanagement.dto.LectureRequest;
import com.aslan.academymanagement.dto.LectureResponse;
import com.aslan.academymanagement.repository.MemberRepository;
import com.aslan.academymanagement.service.lecture.LectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/lecture")
@Tag(name = "Lecture", description = "강의 및 시간표 관리 API")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;
    private final MemberRepository memberRepository;

    @PostMapping
    @Operation(summary = "강의 생성", description = "새로운 강의를 생성합니다. 기간(startDate, endDate)을 설정하지 않으면 오늘부터 3개월로 자동 설정됩니다.")
    public ResponseEntity<LectureResponse> createLecture(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody LectureRequest lectureRequest){
        
        Member teacher = getMember(userDetails);
        LectureResponse lecture = lectureService.createLecture(teacher, lectureRequest);
        return ResponseEntity.ok(lecture);
    }

    @GetMapping
    @Operation(summary = "내 강의 목록 조회", description = "로그인한 강사가 개설한 강의 목록을 조회합니다.")
    public ResponseEntity<List<LectureResponse>> retrieveMyLectures(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Member teacher = getMember(userDetails);
        List<LectureResponse> lectures = lectureService.retrieveMyLectures(teacher);
        return ResponseEntity.ok(lectures);
    }

    @GetMapping("/all")
    @Operation(summary = "전체 강의 목록 조회 (관리자용)", description = "등록된 전체 강의 목록을 조회합니다.")
    public ResponseEntity<List<LectureResponse>> retrieveAllLectures() {
        List<LectureResponse> lectures = lectureService.retrieveAllLectures();
        return ResponseEntity.ok(lectures);
    }

    @GetMapping("/{lectureId}")
    @Operation(summary = "강의 상세 조회", description = "특정 ID에 해당하는 강의 정보를 조회합니다.")
    public ResponseEntity<LectureResponse> retrieveLecture(@PathVariable Long lectureId) {
        LectureResponse response = lectureService.retrieveLecture(lectureId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/events")
    @Operation(summary = "달력용 강의 이벤트 조회", description = "FullCalendar에 표시할 강의 스케줄 데이터를 반환합니다. 기간(start, end) 및 강사(instructorId)를 지정하여 조회할 수 있습니다.")
    public ResponseEntity<List<LectureEventDto>> getLectureEvents(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "조회 시작일 (YYYY-MM-DD)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "조회 종료일 (YYYY-MM-DD)", example = "2026-01-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @Parameter(description = "강사 ID (원장용 필터링)", example = "1")
            @RequestParam(required = false) Long instructorId,
            @Parameter(description = "전체 조회 여부 (원장용)", example = "true")
            @RequestParam(required = false) Boolean viewAll
    ) {
        Member loginUser = getMember(userDetails);
        List<LectureEventDto> events = lectureService.getLectureEvents(loginUser, start, end, instructorId, viewAll);
        return ResponseEntity.ok(events);
    }

    private Member getMember(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return memberRepository.findByGoogleEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다."));
    }
}
