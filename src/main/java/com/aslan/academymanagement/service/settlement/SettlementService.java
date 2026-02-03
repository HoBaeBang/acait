package com.aslan.academymanagement.service.settlement;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.SettlementDetailResponse;
import com.aslan.academymanagement.dto.SettlementForecastResponse;
import com.aslan.academymanagement.dto.SettlementResponse;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface SettlementService {
    void calculateMonthlySettlement(String yearMonth);

    // 강사 본인의 정산 실행
    void calculateMySettlement(Member instructor, String yearMonth);

    List<SettlementResponse> getMonthlySettlements(Member admin, String yearMonth);

    List<SettlementResponse> getMySettlements(Member instructor, String yearMonth);

    ByteArrayInputStream exportSettlementToExcel(Member admin, String yearMonth);

    List<SettlementDetailResponse> getSettlementDetails(Member member, Long settlementId);

    SettlementForecastResponse getSettlementForecast(Member instructor, String yearMonth);
}
