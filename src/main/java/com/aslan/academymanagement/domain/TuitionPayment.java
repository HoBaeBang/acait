package com.aslan.academymanagement.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tuition_payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TuitionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "target_month", nullable = false)
    private String yearMonth; // 납부 대상 월 (YYYY-MM)

    @Column(columnDefinition = "TEXT")
    private String memo;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TuitionPayment(Student student, BigDecimal amount, LocalDate paymentDate, String yearMonth, String memo) {
        this.student = student;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.yearMonth = yearMonth;
        this.memo = memo;
    }
}
