package com.aslan.academymanagement.repository;

import com.aslan.academymanagement.domain.LectureRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LectureRecordRepository extends JpaRepository<LectureRecord, Long> {
}
