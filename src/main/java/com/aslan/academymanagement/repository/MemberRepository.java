package com.aslan.academymanagement.repository;

import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByGoogleEmail(String googleEmail);
    
    // 학원별 회원 조회
    List<Member> findAllByAcademy(Academy academy);
    
    // 학원별 + 상태별 회원 수 카운트 (인원 제한 체크용)
    long countByAcademyAndStatus(Academy academy, MemberStatus status);
}
