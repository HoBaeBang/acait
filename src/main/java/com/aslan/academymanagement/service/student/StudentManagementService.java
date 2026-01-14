package com.aslan.academymanagement.service.student;

import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.dto.StudentRequest;

import java.util.List;

public interface StudentManagementService {

    Student registerStudent(StudentRequest request);

    Student getStudent(String studentNumber);

    Student updateStudent(String studentNumber, StudentRequest request);

    // 출석 체크 및 성적 입력은 LectureRecord로 이동 예정이므로 일단 제거하거나 유지 (추후 결정)
    // void checkAttendance(String studentId);
    // void updateScore(String studentId, Double score);

    // 우수 학생 조회도 기준이 변경될 수 있으므로 일단 유지
    List<Student> getTopStudents();

    String getDivisionType();
}
