package com.aslan.academymanagement.repository;

import com.aslan.academymanagement.domain.LearningHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearningHistoryRepository extends JpaRepository<LearningHistory, Long> {
}
