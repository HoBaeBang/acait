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

    // year_month는 예약어일 수 있으므로 백틱으로 감싸거나 이름을 변경하는 것이 안전함
    // 하지만 MySQL 8.0에서 year_month는 예약어가 아님.
    // 에러 로그를 다시 보면 'year_month varchar(7) not null,' 부분에서 에러가 났다고 하는데,
    // 이는 앞의 컬럼 정의에서 콤마(,)가 빠졌거나 뭔가 잘못되었을 수 있음.
    // 하지만 코드를 보면 정상임.

    // 혹시 모르니 컬럼명을 `settlement_year_month`로 변경해보겠습니다.
    @Column(name = "settlement_year_month", nullable = false, length = 7)
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
