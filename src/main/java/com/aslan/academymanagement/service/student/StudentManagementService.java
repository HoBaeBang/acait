package com.aslan.academymanagement.service.student;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.dto.StudentRequest;

import java.util.List;

public interface StudentManagementService {

    // 학원별 학번 생성을 위해 Member(강사) 정보 필요
    Student registerStudent(Member teacher, StudentRequest request);

    Student getStudent(String studentNumber);

    Student updateStudent(String studentNumber, StudentRequest request);

    void dischargeStudent(String studentNumber);

    List<Student> getTopStudents();
}
