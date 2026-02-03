package com.aslan.academymanagement.repository;


import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.domain.enums.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentNumber(String studentNumber);

    List<Student> findByGrade(Grade grade);

    List<Student> findByNameContaining(String name);

    boolean existsByStudentNumber(String studentNumber);

    // 학원별 학생 목록 조회 (원장용)
    List<Student> findAllByAcademy(Academy academy);

    // 강사별 수강생 목록 조회 (강사용 - 중복 제거)
    @Query("SELECT DISTINCT s FROM Student s " +
            "JOIN LectureStudent ls ON s.id = ls.student.id " +
            "JOIN Lecture l ON ls.lecture.id = l.id " +
            "WHERE l.teacher = :teacher")
    List<Student> findAllByTeacher(@Param("teacher") Member teacher);
}
