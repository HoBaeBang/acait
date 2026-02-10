package com.aslan.academymanagement.service.admin;

import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.enums.MemberStatus;
import com.aslan.academymanagement.domain.enums.Role;
import com.aslan.academymanagement.dto.MemberResponse;
import com.aslan.academymanagement.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> getInstructors(Member owner) {
        Academy academy = owner.getAcademy();
        return memberRepository.findAllByAcademy(academy).stream()
                .filter(member -> member.getRole() == Role.ROLE_INSTRUCTOR || member.getRole() == Role.ROLE_MANAGER)
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public void approveInstructor(Member owner, Long memberId) {
        Academy academy = owner.getAcademy();
        Member instructor = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));

        if (!instructor.getAcademy().getId().equals(academy.getId())) {
            throw new IllegalArgumentException("다른 학원의 강사를 승인할 수 없습니다.");
        }

        if (instructor.getStatus() != MemberStatus.PENDING) {
            throw new IllegalArgumentException("승인 대기 상태인 회원만 승인할 수 있습니다.");
        }

        long activeCount = memberRepository.countByAcademyAndStatus(academy, MemberStatus.ACTIVE);
        if (activeCount >= academy.getMaxMembers()) {
            throw new IllegalStateException("PLAN_LIMIT");
        }

        instructor.approve();
        memberRepository.save(instructor);
    }

    @Override
    public MemberResponse updateMemberRole(Member owner, Long memberId, Role newRole) {
        Academy academy = owner.getAcademy();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));

        if (!member.getAcademy().getId().equals(academy.getId())) {
            throw new IllegalArgumentException("다른 학원의 회원 역할을 변경할 수 없습니다.");
        }

        // 원장 본인의 역할은 변경 불가
        if (member.getId().equals(owner.getId())) {
            throw new IllegalArgumentException("원장 본인의 역할은 변경할 수 없습니다.");
        }

        // 변경 가능한 역할인지 확인 (강사 <-> 실장)
        if (newRole != Role.ROLE_INSTRUCTOR && newRole != Role.ROLE_MANAGER) {
            throw new IllegalArgumentException("변경할 수 없는 역할입니다.");
        }

        member.updateRole(newRole);
        memberRepository.save(member);
        
        log.info("✅ 역할 변경 완료: {} -> {}", member.getName(), newRole);

        return MemberResponse.from(member);
    }
}
