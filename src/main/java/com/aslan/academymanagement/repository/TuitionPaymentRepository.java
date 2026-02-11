package com.aslan.academymanagement.repository;

import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.domain.TuitionPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TuitionPaymentRepository extends JpaRepository<TuitionPayment, Long> {
    List<TuitionPayment> findAllByStudentAndYearMonth(Student student, String yearMonth);
}
