package com.aslan.academymanagement.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "student_balances",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_student_target_month",
            columnNames = {"student_id", "target_month"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "target_month", nullable = false)
    private String yearMonth; // 대상 월 (YYYY-MM)

    @Column(name = "paid_amount", nullable = false)
    private BigDecimal paidAmount; // 해당 월에 납부한 총액

    @Column(name = "used_amount", nullable = false)
    private BigDecimal usedAmount; // 해당 월에 수업으로 소진한 총액

    @Column(name = "carry_over_amount", nullable = false)
    private BigDecimal carryOverAmount; // 전월에서 이월된 금액

    @Column(name = "current_balance", nullable = false)
    private BigDecimal currentBalance; // 현재 잔액 (이월금 + 납부액 - 사용액)

    @Builder
    public StudentBalance(Student student, String yearMonth, BigDecimal paidAmount, BigDecimal usedAmount, BigDecimal carryOverAmount, BigDecimal currentBalance) {
        this.student = student;
        this.yearMonth = yearMonth;
        this.paidAmount = paidAmount;
        this.usedAmount = usedAmount;
        this.carryOverAmount = carryOverAmount;
        this.currentBalance = currentBalance;
    }

    public void updateAmounts(BigDecimal paidAmount, BigDecimal usedAmount, BigDecimal carryOverAmount) {
        this.paidAmount = paidAmount;
        this.usedAmount = usedAmount;
        this.carryOverAmount = carryOverAmount;
        this.currentBalance = this.carryOverAmount.add(this.paidAmount).subtract(this.usedAmount);
    }
}
