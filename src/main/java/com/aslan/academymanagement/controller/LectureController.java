package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.dto.LectureEventDto;
import com.aslan.academymanagement.dto.LectureRequest;
import com.aslan.academymanagement.dto.LectureResponse;
import com.aslan.academymanagement.service.lecture.LectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/lecture")
@Tag(name = "Lecture", description = "강의 관련 API")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    @PostMapping
    @Operation(summary = "강의 생성", description = "강의 정보를 생성합니다.")
    public ResponseEntity<LectureResponse> createLecture(@RequestBody LectureRequest lectureRequest){
        LectureResponse lecture = lectureService.createLecture(lectureRequest);
        return ResponseEntity.ok(lecture);
    }

    @GetMapping
    @Operation(summary = "강의 목록 전체 조회", description = "등록된 전체 강의 목록을 조회합니다.")
    public ResponseEntity<List<LectureResponse>> retrieveLectures() {
        List<LectureResponse> lectures = lectureService.retrieveLecture();
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
