package com.aslan.academymanagement.service.settlement;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.SettlementDetailResponse;
import com.aslan.academymanagement.dto.SettlementResponse;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface SettlementService {
    void calculateMonthlySettlement(String yearMonth);

    List<SettlementResponse> getMonthlySettlements(Member admin, String yearMonth);

    List<SettlementResponse> getMySettlements(Member instructor);

    ByteArrayInputStream exportSettlementToExcel(Member admin, String yearMonth);

    // 정산 상세 내역 조회
    List<SettlementDetailResponse> getSettlementDetails(Member admin, Long settlementId);
}
