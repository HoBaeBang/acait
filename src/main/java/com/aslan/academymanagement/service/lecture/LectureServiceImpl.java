package com.aslan.academymanagement.service.lecture;

import com.aslan.academymanagement.annotation.Loggable;
import com.aslan.academymanagement.domain.Lecture;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.Schedule;
import com.aslan.academymanagement.dto.LectureEventDto;
import com.aslan.academymanagement.dto.LectureRequest;
import com.aslan.academymanagement.dto.LectureResponse;
import com.aslan.academymanagement.repository.LectureRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;

    @Transactional
    @Loggable
    @Override
    public LectureResponse createLecture(Member teacher, LectureRequest req) {
        Lecture lecture = req.toLecture();

        if (req.getStartDate() != null) {
            lecture.setStartDate(req.getStartDate());
        } else {
            lecture.setStartDate(LocalDate.now());
        }

        if (req.getEndDate() != null) {
            lecture.setEndDate(req.getEndDate());
        } else {
            lecture.setEndDate(lecture.getStartDate().plusMonths(3));
        }

        lecture.setTeacher(teacher);

        List<Schedule> schedules = req.toSchedules();
        for (Schedule schedule : schedules) {
            lecture.addSchedule(schedule);
        }
        Lecture saved = lectureRepository.save(lecture);
        saved.getSchedules().size();

        return LectureResponse.from(saved);
    }

    @Transactional
    @Loggable
    @Override
    public List<LectureResponse> retrieveAllLectures() {
        return lectureRepository.findAll()
                .stream()
                .map(LectureResponse::from)
                .toList();
    }

    @Transactional
    @Loggable
    @Override
    public List<LectureResponse> retrieveMyLectures(Member teacher) {
        return lectureRepository.findAllByTeacher(teacher)
                .stream()
                .map(LectureResponse::from)
                .toList();
    }

    @Transactional
    @Override
    public LectureResponse retrieveLecture(Long lectureId) {
        return lectureRepository.findById(lectureId)
                .map(LectureResponse::from)
                .orElse(null);
    }

    @Transactional
    @Override
    public List<LectureEventDto> getLectureEvents(LocalDate start, LocalDate end) {
        // 조회 기간이 없으면 이번 주 월~일로 설정
        if (start == null) start = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        if (end == null) end = start.plusDays(6);

        LocalDate finalStart = start;
        LocalDate finalEnd = end;

        // 모든 강의를 가져와서, 요청된 기간(start~end)에 해당하는 반복 일정만 생성하여 반환
        return lectureRepository.findAll().stream()
                .flatMap(lecture -> LectureEventDto.from(lecture, finalStart, finalEnd).stream())
                .collect(Collectors.toList());
    }
}
