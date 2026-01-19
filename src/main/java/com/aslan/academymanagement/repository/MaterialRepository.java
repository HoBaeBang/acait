package com.aslan.academymanagement.repository;

import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.domain.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    // 제목으로 검색 (공용 교재 OR 내 학원 교재)
    @Query("SELECT m FROM Material m " +
            "WHERE m.title LIKE %:keyword% " +
            "AND (m.academy IS NULL OR m.academy = :academy)")
    List<Material> searchByTitle(@Param("keyword") String keyword, @Param("academy") Academy academy);
}
