package com.aslan.academymanagement.repository;

import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Optional<Settlement> findByInstructorAndYearMonth(Member instructor, String yearMonth);
    List<Settlement> findAllByAcademyAndYearMonth(Academy academy, String yearMonth);
}
