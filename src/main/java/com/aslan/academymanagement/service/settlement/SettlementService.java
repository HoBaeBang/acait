package com.aslan.academymanagement.service.settlement;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.SettlementDetailResponse;
import com.aslan.academymanagement.dto.SettlementForecastResponse;
import com.aslan.academymanagement.dto.SettlementResponse;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface SettlementService {
    void calculateMonthlySettlement(String yearMonth);

    List<SettlementResponse> getMonthlySettlements(Member admin, String yearMonth);

    List<SettlementResponse> getMySettlements(Member instructor, String yearMonth);

    ByteArrayInputStream exportSettlementToExcel(Member admin, String yearMonth);

    List<SettlementDetailResponse> getSettlementDetails(Member member, Long settlementId);

    // 예상 정산 금액 조회
    SettlementForecastResponse getSettlementForecast(Member instructor, String yearMonth);
}
