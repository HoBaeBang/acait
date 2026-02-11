package com.aslan.academymanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TuitionPaymentRequest {
    @NotNull(message = "금액은 필수입니다.")
    private BigDecimal amount;

    @NotNull(message = "납부일은 필수입니다.")
    private LocalDate paymentDate;

    @NotNull(message = "대상 월은 필수입니다. (YYYY-MM)")
    private String yearMonth;

    private String memo;
}
