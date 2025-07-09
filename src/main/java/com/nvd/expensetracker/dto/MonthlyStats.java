package com.nvd.expensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MonthlyStats {
    private String month;
    private BigDecimal totalAmount;
}
