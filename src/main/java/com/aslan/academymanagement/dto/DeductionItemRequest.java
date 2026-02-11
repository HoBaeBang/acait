package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.enums.DeductionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeductionItemRequest {
    @NotBlank(message = "항목명은 필수입니다.")
    private String name;

    @NotNull(message = "공제 타입은 필수입니다.")
    private DeductionType type;

    @NotNull(message = "값은 필수입니다.")
    private BigDecimal value;
}
