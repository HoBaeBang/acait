package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.LectureEventDto;
import com.aslan.academymanagement.dto.LectureRequest;
import com.aslan.academymanagement.dto.LectureResponse;
import com.aslan.academymanagement.repository.MemberRepository;
import com.aslan.academymanagement.service.lecture.LectureService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Lecture", description = "강의 관련 API")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;
    private final MemberRepository memberRepository;

    @PostMapping
    @Operation(summary = "강의 생성", description = "강의 정보를 생성합니다.")
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
    @Operation(summary = "강의 조회", description = "특정 id에 해당하는 강의 정보를 조회합니다.")
    public ResponseEntity<LectureResponse> retrieveLecture(@PathVariable Long lectureId) {
        LectureResponse response = lectureService.retrieveLecture(lectureId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/events")
    @Operation(summary = "달력용 강의 이벤트 조회", description = "FullCalendar에 표시할 강의 스케줄 데이터를 반환합니다.")
    public ResponseEntity<List<LectureEventDto>> getLectureEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        List<LectureEventDto> events = lectureService.getLectureEvents(start, end);
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
