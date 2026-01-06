package com.aslan.academymanagement.service.lecture;

import com.aslan.academymanagement.dto.LectureRequest;
import com.aslan.academymanagement.dto.LectureResponse;

import java.util.List;

public interface LectureService {
    public LectureResponse createLecture(LectureRequest req);

    List<LectureResponse> retrieveLecture();

    LectureResponse retrieveLecture(Long lectureId);
}
