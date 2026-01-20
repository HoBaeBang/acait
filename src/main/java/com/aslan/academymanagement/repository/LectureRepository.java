package com.aslan.academymanagement.repository;

import com.aslan.academymanagement.domain.Lecture;
import com.aslan.academymanagement.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    Optional<Lecture> findById(Long id);

    // 특정 강사의 강의 목록 조회
    List<Lecture> findAllByTeacher(Member teacher);

    // 특정 학생이 수강 중인 강의 목록 조회 (LectureStudent 테이블 조인)
    @Query("SELECT l FROM Lecture l JOIN LectureStudent ls ON l.id = ls.lecture.id WHERE ls.student.id = :studentId")
    List<Lecture> findAllByStudentId(@Param("studentId") Long studentId);
}
