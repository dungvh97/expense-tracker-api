package com.nvd.expensetracker.repository;

import com.nvd.expensetracker.model.Budget;
import com.nvd.expensetracker.model.Expense;
import com.nvd.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUser(User user);
}
