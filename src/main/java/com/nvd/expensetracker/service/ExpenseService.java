package com.nvd.expensetracker.service;

import com.nvd.expensetracker.dto.ExpenseRequest;
import com.nvd.expensetracker.model.Category;
import com.nvd.expensetracker.model.Expense;
import com.nvd.expensetracker.model.User;
import com.nvd.expensetracker.repository.CategoryRepository;
import com.nvd.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.nvd.expensetracker.dto.CategoryExpenseStats;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepo;
    private final CategoryRepository categoryRepo;

    public List<Expense> filterExpenses(User user, Long categoryId, LocalDate startDate, LocalDate endDate) {
        List<Expense> all = expenseRepo.findByUser(user);

        return all.stream()
                .filter(e -> categoryId == null || e.getCategory().getId().equals(categoryId))
                .filter(e -> startDate == null || !e.getDate().isBefore(startDate))
                .filter(e -> endDate == null || !e.getDate().isAfter(endDate))
                .collect(toList());
    }

    public BigDecimal getTotalExpense(User user, LocalDate startDate, LocalDate endDate) {
        List<Expense> all = expenseRepo.findByUser(user);

        return all.stream()
                .filter(e -> startDate == null || !e.getDate().isBefore(startDate))
                .filter(e -> endDate == null || !e.getDate().isAfter(endDate))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<CategoryExpenseStats> getStatsByCategory(User user) {
        List<Expense> expenses = expenseRepo.findByUser(user);

        return expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ))
                .entrySet().stream()
                .map(e -> new CategoryExpenseStats(e.getKey(), e.getValue()))
                .collect(toList());
    }

    public BigDecimal getFilteredTotal(User user, Long categoryId, LocalDate startDate, LocalDate endDate) {
        List<Expense> all = expenseRepo.findByUser(user);

        return all.stream()
                .filter(e -> categoryId == null || e.getCategory().getId().equals(categoryId))
                .filter(e -> startDate == null || !e.getDate().isBefore(startDate))
                .filter(e -> endDate == null || !e.getDate().isAfter(endDate))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Expense createExpense(ExpenseRequest request, User user) {
        Category category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Expense expense = Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .date(request.getDate())
                .category(category)
                .user(user)
                .build();

        return expenseRepo.save(expense);
    }

}
