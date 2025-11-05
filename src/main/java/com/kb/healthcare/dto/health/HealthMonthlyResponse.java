package com.kb.healthcare.dto.health;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthMonthlyResponse implements Serializable {
    String monthly;
    Integer steps;
    BigDecimal calories;
    BigDecimal distance;
    String recordKey;
}