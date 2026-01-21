package com.aslan.academymanagement.service.student;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.dto.LectureResponse;
import com.aslan.academymanagement.dto.StudentRequest;
import com.aslan.academymanagement.dto.StudentResponse;

import java.util.List;

public interface StudentManagementService {

    Student registerStudent(Member teacher, StudentRequest request);

    Student getStudent(String studentNumber);

    Student updateStudent(String studentNumber, StudentRequest request);

    void dischargeStudent(String studentNumber);

    List<Student> getTopStudents();

    List<LectureResponse> getEnrolledLectures(String studentNumber);

    // 전체 학생 목록 조회 (학원별)
    List<StudentResponse> getAllStudents(Member teacher);
}
