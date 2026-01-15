package com.aslan.academymanagement.repository;


import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.domain.enums.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentNumber(String studentNumber);

    // Division 필드가 삭제되었으므로 관련 메서드 제거
    // List<Student> findByDivision(Division division);

    List<Student> findByGrade(Grade grade);

    List<Student> findByNameContaining(String name);

    // Division 필드가 삭제되었으므로 쿼리 수정 필요 (일단 제거)
    // @Query("SELECT s FROM Student s WHERE s.division = :division AND s.grade = :grade")
    // List<Student> findByDivisionAndGrade(...)

    boolean existsByStudentNumber(String studentNumber);
}
