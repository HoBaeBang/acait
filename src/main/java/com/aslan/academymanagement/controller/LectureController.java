package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Lecture;
import com.aslan.academymanagement.dto.LectureRequest;
import com.aslan.academymanagement.dto.LectureResponse;
import com.aslan.academymanagement.dto.StudentResponse;
import com.aslan.academymanagement.service.lecture.LectureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/lecture")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    @PostMapping
    public ResponseEntity<LectureResponse> createLecture(@RequestBody LectureRequest lectureRequest){
        LectureResponse lecture = lectureService.createLecture(lectureRequest);
        return ResponseEntity.ok(lecture);
    }

    @GetMapping
    public ResponseEntity<List<LectureResponse>> retrieveLecture() {
        List<LectureResponse> lectures = lectureService.retrieveLecture();
        return ResponseEntity.ok(lectures);
    }
}
