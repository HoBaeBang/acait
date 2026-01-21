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

    // 강사별 정산 내역 조회 (전체 기간)
    List<Settlement> findAllByInstructor(Member instructor);

    // 강사별 정산 내역 조회 (특정 월)
    List<Settlement> findAllByInstructorAndYearMonth(Member instructor, String yearMonth);
}
