package com.aslan.academymanagement.repository;

import com.aslan.academymanagement.domain.Lecture;
import com.aslan.academymanagement.domain.LectureStudent;
import com.aslan.academymanagement.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureStudentRepository extends JpaRepository<LectureStudent, Long> {
    // 특정 강의의 수강생 목록 조회 (Fetch Join으로 성능 최적화 권장)
    List<LectureStudent> findAllByLecture(Lecture lecture);

    // 중복 등록 방지용 조회
    boolean existsByLectureAndStudent(Lecture lecture, Student student);

    // 등록 취소용 조회
    Optional<LectureStudent> findByLectureAndStudent(Lecture lecture, Student student);
}
