package com.aslan.academymanagement.repository;

import com.aslan.academymanagement.domain.Schedule;
import com.aslan.academymanagement.domain.ScheduleException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleExceptionRepository extends JpaRepository<ScheduleException, Long> {

    // 특정 기간 내의 예외 사항 조회 (originalDate 기준)
    @Query("SELECT se FROM ScheduleException se " +
            "WHERE se.originalDate BETWEEN :startDate AND :endDate")
    List<ScheduleException> findByOriginalDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 특정 스케줄의 예외 사항 조회
    List<ScheduleException> findBySchedule(Schedule schedule);
}
