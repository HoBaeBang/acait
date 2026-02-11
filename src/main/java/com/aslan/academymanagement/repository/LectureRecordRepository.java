package com.aslan.academymanagement.repository;

import com.aslan.academymanagement.domain.LectureRecord;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.domain.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LectureRecordRepository extends JpaRepository<LectureRecord, Long> {

    // 특정 기간 내의 유효한 수업 기록 조회 (출석, 지각, 보강 완료)
    @Query("SELECT lr FROM LectureRecord lr " +
            "JOIN FETCH lr.lecture l " +
            "JOIN FETCH lr.student s " +
            "JOIN FETCH l.teacher m " +
            "JOIN FETCH l.academy a " +
            "WHERE lr.date BETWEEN :startDate AND :endDate " +
            "AND lr.attendanceStatus IN :statuses")
    List<LectureRecord> findSettlementTargets(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<AttendanceStatus> statuses
    );

    // 특정 강사의 특정 기간 내 유효 수업 기록 조회 (정산 상세용)
    @Query("SELECT lr FROM LectureRecord lr " +
            "JOIN FETCH lr.lecture l " +
            "JOIN FETCH lr.student s " +
            "WHERE l.teacher = :instructor " +
            "AND lr.date BETWEEN :startDate AND :endDate " +
            "AND lr.attendanceStatus IN :statuses")
    List<LectureRecord> findByInstructorAndDateBetweenAndStatusIn(
            @Param("instructor") Member instructor,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<AttendanceStatus> statuses
    );

    // 특정 학생의 특정 기간 내 유효 수업 기록 조회 (잔액 계산용)
    @Query("SELECT lr FROM LectureRecord lr " +
            "JOIN FETCH lr.lecture l " +
            "WHERE lr.student = :student " +
            "AND lr.date BETWEEN :startDate AND :endDate " +
            "AND lr.attendanceStatus IN :statuses")
    List<LectureRecord> findByStudentAndDateBetweenAndStatusIn(
            @Param("student") Student student,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<AttendanceStatus> statuses
    );
}
