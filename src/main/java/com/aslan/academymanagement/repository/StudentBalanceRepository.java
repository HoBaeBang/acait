package com.aslan.academymanagement.repository;

import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.domain.StudentBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentBalanceRepository extends JpaRepository<StudentBalance, Long> {
    Optional<StudentBalance> findByStudentAndYearMonth(Student student, String yearMonth);
}
