package com.aslan.academymanagement.service.settlement;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.SettlementResponse;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface SettlementService {
    // 월별 정산 데이터 생성 및 갱신 (배치 또는 트리거로 실행)
    void calculateMonthlySettlement(String yearMonth);

    // 특정 학원의 월별 정산 현황 조회 (대시보드용)
    List<SettlementResponse> getMonthlySettlements(Member admin, String yearMonth);

    // 내 정산 내역 조회 (강사용)
    List<SettlementResponse> getMySettlements(Member instructor);

    // 정산 내역 엑셀 다운로드
    ByteArrayInputStream exportSettlementToExcel(Member admin, String yearMonth);
}
