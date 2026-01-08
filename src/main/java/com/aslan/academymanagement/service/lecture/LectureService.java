package com.aslan.academymanagement.service.lecture;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.LectureEventDto;
import com.aslan.academymanagement.dto.LectureRequest;
import com.aslan.academymanagement.dto.LectureResponse;

import java.util.List;

public interface LectureService {
    // Member(강사) 정보를 파라미터로 받도록 수정
    LectureResponse createLecture(Member teacher, LectureRequest req);

    // 전체 강의 조회 (관리자용)
    List<LectureResponse> retrieveAllLectures();

    // 내 강의 조회 (강사용)
    List<LectureResponse> retrieveMyLectures(Member teacher);

    LectureResponse retrieveLecture(Long lectureId);

    List<LectureEventDto> getLectureEvents(); // 달력용 데이터 조회
}
