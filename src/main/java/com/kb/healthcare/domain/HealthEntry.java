package com.kb.healthcare.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "health_entry",
        uniqueConstraints = @UniqueConstraint(name = "uq_entry", columnNames = {"recordKey", "startedAtKst", "endedAtKst"}),
        indexes = @Index(columnList = "recordKey"))
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthEntry extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String recordKey;

    @Column(nullable = false)
    private LocalDateTime startedAtKst;

    @Column(nullable = false)
    private LocalDateTime endedAtKst;

    @Column(nullable = false)
    private Integer steps;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal distanceKm;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal caloriesKcal;

    public HealthEntry(String recordKey, LocalDateTime startedAtKst, LocalDateTime endedAtKst,
                       Integer steps, BigDecimal distanceKm, BigDecimal caloriesKcal) {
        this.recordKey = recordKey;
        this.startedAtKst = startedAtKst;
        this.endedAtKst = endedAtKst;
        this.steps = steps;
        this.distanceKm = distanceKm;
        this.caloriesKcal = caloriesKcal;
    }
}