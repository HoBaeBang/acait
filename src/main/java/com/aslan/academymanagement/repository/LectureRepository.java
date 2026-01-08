package com.aslan.academymanagement.repository;

import com.aslan.academymanagement.domain.Lecture;
import com.aslan.academymanagement.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    Optional<Lecture> findById(Long id);

    // 특정 강사의 강의 목록 조회
    List<Lecture> findAllByTeacher(Member teacher);
}
