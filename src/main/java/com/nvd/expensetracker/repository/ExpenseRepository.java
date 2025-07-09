package com.nvd.expensetracker.repository;

import com.nvd.expensetracker.model.Expense;
import com.nvd.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

import static com.nvd.expensetracker.query.ExpenseQueries.FILTER_BY_CATEGORY_AND_DATE;


public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUser(User user);

    @Query(FILTER_BY_CATEGORY_AND_DATE)
    List<Expense> filterByCategoryAndDate(
            @Param("user") User user,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}