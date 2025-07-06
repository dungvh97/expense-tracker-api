package com.nvd.expensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CategoryExpenseStats {
    private String category;
    private BigDecimal total;
}
