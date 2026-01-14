package com.aslan.academymanagement.repository;

import com.aslan.academymanagement.domain.Academy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademyRepository extends JpaRepository<Academy, Long> {
    Optional<Academy> findByInviteCode(String inviteCode);
}
