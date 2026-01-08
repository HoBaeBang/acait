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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/lecture")
@Tag(name = "Lecture", description = "강의 관련 API")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;
    private final MemberRepository memberRepository; // 임시로 Repository 직접 사용 (추후 Service로 이동 권장)

    @PostMapping
    @Operation(summary = "강의 생성", description = "강의 정보를 생성합니다.")
    public ResponseEntity<LectureResponse> createLecture(
            @AuthenticationPrincipal UserDetails userDetails, // Spring Security가 주입해주는 유저 정보
            @RequestBody LectureRequest lectureRequest){
        
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        // 1. 현재 로그인한 유저의 이메일(username) 가져오기
        String email = userDetails.getUsername();
        
        // 2. DB에서 Member 엔티티 조회
        Member teacher = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. email=" + email));

        // 3. 서비스 호출 시 강사 정보 전달
        LectureResponse lecture = lectureService.createLecture(teacher, lectureRequest);
        return ResponseEntity.ok(lecture);
    }

    @GetMapping
    @Operation(summary = "내 강의 목록 조회", description = "로그인한 강사가 개설한 강의 목록을 조회합니다.")
    public ResponseEntity<List<LectureResponse>> retrieveMyLectures(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            // 로그인하지 않은 경우 빈 목록 반환하거나 401 에러 (여기서는 일단 빈 목록)
            // 혹은 전체 목록을 보여줄지 정책 결정 필요. 현재는 "내 강의" 조회이므로 에러가 맞음.
             throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        String email = userDetails.getUsername();
        Member teacher = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다."));

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

    // FullCalendar용 이벤트 조회 API 추가
    @GetMapping("/events")
    @Operation(summary = "달력용 강의 이벤트 조회", description = "FullCalendar에 표시할 강의 스케줄 데이터를 반환합니다.")
    public ResponseEntity<List<LectureEventDto>> getLectureEvents() {
        List<LectureEventDto> events = lectureService.getLectureEvents();
        return ResponseEntity.ok(events);
    }
}
