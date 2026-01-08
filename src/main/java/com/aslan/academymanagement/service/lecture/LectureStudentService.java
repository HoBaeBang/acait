package com.aslan.academymanagement.service.lecture;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.StudentResponse;

import java.util.List;

public interface LectureStudentService {
    // 학생 등록
    void registerStudent(Member teacher, Long lectureId, Long studentId);

    // 학생 제외
    void removeStudent(Member teacher, Long lectureId, Long studentId);

    // 수강생 목록 조회
    List<StudentResponse> getStudentsByLecture(Member teacher, Long lectureId);
}
