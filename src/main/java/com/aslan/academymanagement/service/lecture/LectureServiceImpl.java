package com.aslan.academymanagement.service.lecture;

import com.aslan.academymanagement.annotation.Loggable;
import com.aslan.academymanagement.domain.Lecture;
import com.aslan.academymanagement.domain.LectureSchedule;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.LectureEventDto;
import com.aslan.academymanagement.dto.LectureRequest;
import com.aslan.academymanagement.dto.LectureResponse;
import com.aslan.academymanagement.repository.LectureRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        // 강의 추가
        Lecture lecture = req.toLecture();

        // 강사 정보 설정
        lecture.setTeacher(teacher);

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
    public List<LectureEventDto> getLectureEvents() {
        // 모든 강의를 가져와서 -> 각각의 스케줄을 -> 달력 이벤트(이번 주 기준)로 변환해서 -> 하나의 리스트로 합침
        return lectureRepository.findAll().stream()
                .flatMap(lecture -> LectureEventDto.from(lecture).stream())
                .collect(Collectors.toList());
    }
}
