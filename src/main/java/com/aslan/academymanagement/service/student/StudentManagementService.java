package com.aslan.academymanagement.service.student;

import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.dto.StudentRequest;

import java.util.List;

public interface StudentManagementService {

    Student registerStudent(StudentRequest request);

    Student getStudent(String studentNumber);

    Student updateStudent(String studentNumber, StudentRequest request);

    // 학생 퇴원 처리 (논리 삭제)
    void dischargeStudent(String studentNumber);

    // 우수 학생 조회도 기준이 변경될 수 있으므로 일단 유지
    List<Student> getTopStudents();

    String getDivisionType();
}
