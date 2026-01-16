package com.aslan.academymanagement.service.settlement;

import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.Settlement;
import com.aslan.academymanagement.domain.enums.Role;
import com.aslan.academymanagement.dto.SettlementResponse;
import com.aslan.academymanagement.repository.SettlementRepository;
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
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;

    @Override
    public void calculateMonthlySettlement(String yearMonth) {
        // TODO: 실제 정산 계산 로직 구현 (LectureRecord 집계)
        // 1. 해당 월의 모든 LectureRecord 조회 (ATTENDED, MAKEUP 상태)
        // 2. 강사별로 그룹화하여 금액 합산 (Lecture.defaultPrice * 횟수)
        // 3. Settlement 엔티티 생성 또는 업데이트
        // 4. 퇴원일 이후 수업 제외 로직 적용
        log.info("💰 {} 월 정산 계산 시작 (아직 미구현)", yearMonth);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getMonthlySettlements(Member admin, String yearMonth) {
        // 권한 체크: 원장(OWNER)만 조회 가능
        if (admin.getRole() != Role.ROLE_OWNER) {
            throw new IllegalArgumentException("정산 현황 조회 권한이 없습니다.");
        }

        Academy academy = admin.getAcademy();
        return settlementRepository.findAllByAcademyAndYearMonth(academy, yearMonth).stream()
                .map(SettlementResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getMySettlements(Member instructor) {
        // 본인의 모든 정산 내역 조회 (최신순 정렬 필요하지만 일단 전체)
        // Repository에 findAllByInstructor 추가 필요
        return List.of(); // 임시 반환
    }
}
