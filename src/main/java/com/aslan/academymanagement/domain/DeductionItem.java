package com.aslan.academymanagement.domain;

import com.aslan.academymanagement.domain.enums.DeductionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Entity
@Table(name = "deduction_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DeductionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deduction_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", nullable = false)
    private Academy academy;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeductionType type;

    @Column(nullable = false)
    private BigDecimal value;

    @Builder
    public DeductionItem(Academy academy, String name, DeductionType type, BigDecimal value) {
        this.academy = academy;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public void update(String name, DeductionType type, BigDecimal value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }
}
