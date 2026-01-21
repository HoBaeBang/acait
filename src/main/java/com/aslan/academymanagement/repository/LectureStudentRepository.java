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
    List<LectureStudent> findAllByLecture(Lecture lecture);

    boolean existsByLectureAndStudent(Lecture lecture, Student student);

    Optional<LectureStudent> findByLectureAndStudent(Lecture lecture, Student student);

    // 강의별 수강생 수 카운트
    long countByLecture(Lecture lecture);
}
