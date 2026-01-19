package com.aslan.academymanagement.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "materials", indexes = {
        @Index(name = "idx_material_title", columnList = "title"),
        @Index(name = "idx_material_academy", columnList = "academy_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Long id;

    // NULL이면 공용 교재, 값이 있으면 해당 학원 전용 교재
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id")
    private Academy academy;

    @Column(length = 20)
    private String isbn;

    @Column(nullable = false)
    private String title;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Material(Academy academy, String isbn, String title, String thumbnailUrl) {
        this.academy = academy;
        this.isbn = isbn;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
    }
}
