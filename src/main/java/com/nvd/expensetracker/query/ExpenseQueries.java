package com.nvd.expensetracker.query;

public class ExpenseQueries {
    public static final String FILTER_BY_CATEGORY_AND_DATE =
            "SELECT e FROM Expense e WHERE e.user = :user " +
                    "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
                    "AND (:startDate IS NULL OR e.date >= :startDate) " +
                    "AND (:endDate IS NULL OR e.date <= :endDate)";
}
