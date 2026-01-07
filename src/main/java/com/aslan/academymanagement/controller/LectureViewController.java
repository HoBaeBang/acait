package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.dto.LectureRequest;
import com.aslan.academymanagement.service.lecture.LectureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/lectures")
@RequiredArgsConstructor
public class LectureViewController {

    private final LectureService lectureService;

    // 강의 목록 및 시간표 페이지
    @GetMapping
    public String getLectureList(Model model) {
        log.info(">>> 강의 목록 페이지 요청 들어옴!");
        return "lecture/list"; // templates/lecture/list.html
    }

    // 강의 생성 페이지
    @GetMapping("/new")
    public String createLectureForm(Model model) {
        log.info(">>> 강의 생성 페이지 요청 들어옴!");
        model.addAttribute("lectureRequest", new LectureRequest());
        return "lecture/create"; // templates/lecture/create.html
    }

    // 강의 생성 처리
    @PostMapping
    public String createLecture(@ModelAttribute LectureRequest lectureRequest) {
        log.info(">>> 강의 생성 요청 들어옴: {}", lectureRequest.getTitle());
        lectureService.createLecture(lectureRequest);
        return "redirect:/lectures"; // 생성 후 목록 페이지로 이동
    }
}
