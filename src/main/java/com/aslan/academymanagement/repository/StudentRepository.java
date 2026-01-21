package com.aslan.academymanagement.repository;


import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.domain.enums.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentNumber(String studentNumber);

    List<Student> findByGrade(Grade grade);

    List<Student> findByNameContaining(String name);

    boolean existsByStudentNumber(String studentNumber);

    // 학원별 학생 목록 조회
    List<Student> findAllByAcademy(Academy academy);
}
