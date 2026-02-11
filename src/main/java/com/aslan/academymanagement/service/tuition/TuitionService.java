package com.aslan.academymanagement.service.tuition;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.StudentBalanceResponse;
import com.aslan.academymanagement.dto.TuitionPaymentRequest;

public interface TuitionService {
    void createPayment(Member manager, Long studentId, TuitionPaymentRequest request);
    StudentBalanceResponse getBalance(Member manager, Long studentId, String yearMonth);
    void calculateBalance(Long studentId, String yearMonth); // 정산 시 호출될 내부 로직
}
