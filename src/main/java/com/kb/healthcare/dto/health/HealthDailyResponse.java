package com.kb.healthcare.dto.health;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthDailyResponse implements Serializable {
    private LocalDate daily;
    private int steps;
    private BigDecimal calories;
    private BigDecimal distance;
    private String recordKey;
}