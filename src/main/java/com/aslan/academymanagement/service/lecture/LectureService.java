package com.aslan.academymanagement.service.lecture;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.LectureEventDto;
import com.aslan.academymanagement.dto.LectureRequest;
import com.aslan.academymanagement.dto.LectureResponse;

import java.time.LocalDate;
import java.util.List;

public interface LectureService {
    LectureResponse createLecture(Member teacher, LectureRequest req);

    List<LectureResponse> retrieveAllLectures();

    List<LectureResponse> retrieveMyLectures(Member teacher);

    LectureResponse retrieveLecture(Long lectureId);

    // 기간별 달력 데이터 조회 (권한 및 필터링 적용)
    List<LectureEventDto> getLectureEvents(Member loginUser, LocalDate start, LocalDate end, Long instructorId, Boolean viewAll);
}
