package com.aslan.academymanagement.service.lecture;

import com.aslan.academymanagement.dto.LectureRequest;
import com.aslan.academymanagement.dto.LectureResponse;

public interface LectureService {
    public LectureResponse createLecture(LectureRequest req);
}
