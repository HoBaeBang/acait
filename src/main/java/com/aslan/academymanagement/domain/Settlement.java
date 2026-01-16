package com.aslan.academymanagement.domain;

import com.aslan.academymanagement.domain.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", nullable = false)
    private Academy academy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Member instructor;

    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth; // YYYY-MM

    @Column(name = "total_amount")
    private BigDecimal totalAmount; // 세전 총액

    @Column(name = "tax_amount")
    private BigDecimal taxAmount; // 공제액 (3.3%)

    @Column(name = "real_amount")
    private BigDecimal realAmount; // 실지급액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Settlement(Academy academy, Member instructor, String yearMonth, BigDecimal totalAmount, SettlementStatus status) {
        this.academy = academy;
        this.instructor = instructor;
        this.yearMonth = yearMonth;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.status = status != null ? status : SettlementStatus.OPEN;
        calculateTaxAndRealAmount();
    }

    // 세금 및 실지급액 계산 로직 (3.3%)
    public void calculateTaxAndRealAmount() {
        if (this.totalAmount == null || this.totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            this.taxAmount = BigDecimal.ZERO;
            this.realAmount = BigDecimal.ZERO;
            return;
        }

        // 3.3% 계산 (원단위 절사)
        BigDecimal taxRate = new BigDecimal("0.033");
        this.taxAmount = this.totalAmount.multiply(taxRate).setScale(0, java.math.RoundingMode.FLOOR);
        this.realAmount = this.totalAmount.subtract(this.taxAmount);
    }

    public void updateTotalAmount(BigDecimal totalAmount) {
        if (this.status == SettlementStatus.CLOSED) {
            throw new IllegalStateException("마감된 정산은 수정할 수 없습니다.");
        }
        this.totalAmount = totalAmount;
        calculateTaxAndRealAmount();
    }

    public void close() {
        this.status = SettlementStatus.CLOSED;
    }
}
