package com.aslan.academymanagement.service.lecture;

import com.aslan.academymanagement.annotation.Loggable;
import com.aslan.academymanagement.domain.Lecture;
import com.aslan.academymanagement.domain.LectureSchedule;
import com.aslan.academymanagement.dto.LectureRequest;
import com.aslan.academymanagement.dto.LectureResponse;
import com.aslan.academymanagement.repository.LectureRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;

    @Transactional
    @Loggable
    @Override
    public LectureResponse createLecture(LectureRequest req) {
        // 강의 추가
        Lecture lecture = req.toLecture();
        // 스케줄 추출
        List<LectureSchedule> lectureSchedules = req.toLectureSchedules();
        // 스케줄 추가
        for (LectureSchedule lectureSchedule : lectureSchedules) {
            lecture.addSchedule(lectureSchedule);
        }
        // 강의 및 스케줄 저장
        Lecture saved = lectureRepository.save(lecture);

        // LAZY 로딩된 schedules 컬렉션을 강제로 초기화
        saved.getSchedules().size();

        return LectureResponse.from(saved);
    }

    @Transactional
    @Loggable
    @Override
    public List<LectureResponse> retrieveLecture() {
        return lectureRepository.findAll()
                .stream()
                .map(LectureResponse::from)
                .toList();
    }
}
