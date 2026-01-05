package com.aslan.academymanagement.repository;


import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.domain.enums.Division;
import com.aslan.academymanagement.domain.enums.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentId(String studentId);

    List<Student> findByDivision(Division division);

    List<Student> findByGrade(Grade grade);

    List<Student> findByNameContaining(String name);

    @Query("SELECT s FROM Student s WHERE s.division = :division AND s.grade = :grade")
    List<Student> findByDivisionAndGrade(
            @Param("division") Division division,
            @Param("grade") Grade grade
    );

    @Query("SELECT s FROM Student s WHERE s.division = :division AND s.averageScore >= :minScore")
    List<Student> findHighAchievers(
            @Param("division") Division division,
            @Param("minScore") Double minScore
    );

    boolean existsByStudentId(String studentId);
}
