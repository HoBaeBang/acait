package com.aslan.academymanagement.service.admin;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.enums.Role;
import com.aslan.academymanagement.dto.MemberResponse;

import java.util.List;

public interface AdminService {
    List<MemberResponse> getInstructors(Member owner);
    void approveInstructor(Member owner, Long memberId);
    MemberResponse updateMemberRole(Member owner, Long memberId, Role newRole);
}
