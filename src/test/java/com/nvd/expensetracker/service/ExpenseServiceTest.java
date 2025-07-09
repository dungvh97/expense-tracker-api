package com.nvd.expensetracker.service;

import com.nvd.expensetracker.dto.CategoryExpenseStats;
import com.nvd.expensetracker.dto.ExpenseRequest;
import com.nvd.expensetracker.model.Category;
import com.nvd.expensetracker.model.Expense;
import com.nvd.expensetracker.model.User;
import com.nvd.expensetracker.repository.CategoryRepository;
import com.nvd.expensetracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepo;

    @Mock
    private CategoryRepository categoryRepo;

    @InjectMocks
    private ExpenseService expenseService;

    private User user;
    private Category category;
    private ExpenseRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        category = new Category();
        category.setId(1L);
        category.setName("Food");

        request = new ExpenseRequest(
                "Lunch",
                new BigDecimal("50000"),
                LocalDate.of(2025, 7, 5),
                category.getId()
        );
    }

    @Test
    void createExpense_shouldSaveAndReturnExpense() {
        when(categoryRepo.findById(category.getId())).thenReturn(Optional.of(category));
        when(expenseRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Expense result = expenseService.createExpense(request, user);

        assertThat(result.getAmount()).isEqualTo(request.getAmount());
        assertThat(result.getDescription()).isEqualTo(request.getDescription());
        assertThat(result.getCategory()).isEqualTo(category);
        assertThat(result.getUser()).isEqualTo(user);
    }

    @Test
    void createExpense_shouldThrowException_whenCategoryNotFound() {
        when(categoryRepo.findById(category.getId())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> expenseService.createExpense(request, user));

        assertThat(exception.getMessage()).isEqualTo("Category not found");
    }

    @Test
    void getFilteredTotal_shouldReturnCorrectSum() {
        Expense expense1 = Expense.builder()
                .amount(new BigDecimal("100000"))
                .date(LocalDate.of(2025, 7, 1))
                .category(category)
                .user(user)
                .build();

        Expense expense2 = Expense.builder()
                .amount(new BigDecimal("200000"))
                .date(LocalDate.of(2025, 7, 5))
                .category(category)
                .user(user)
                .build();

        when(expenseRepo.findByUser(user)).thenReturn(List.of(expense1, expense2));

        BigDecimal total = expenseService.getFilteredTotal(
                user,
                category.getId(),
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2025, 7, 10)
        );

        assertThat(total).isEqualTo(new BigDecimal("300000"));
    }

    @Test
    void filterExpenses_shouldReturnMatchingExpenses() {
        Expense e1 = Expense.builder()
                .amount(new BigDecimal("100000"))
                .description("Mua sách")
                .date(LocalDate.of(2025, 7, 6))
                .category(category)
                .user(user)
                .build();

        Expense e2 = Expense.builder()
                .amount(new BigDecimal("50000"))
                .description("Đi chợ")
                .date(LocalDate.of(2025, 7, 7))
                .category(category)
                .user(user)
                .build();

        when(expenseRepo.findByUser(user)).thenReturn(List.of(e1, e2));

        List<Expense> filtered = expenseService.filterExpenses(
                user, category.getId(),
                LocalDate.of(2025, 7, 5),
                LocalDate.of(2025, 7, 6)
        );

        assertThat(filtered).containsExactly(e1);
    }

    @Test
    void getStatsByCategory_shouldReturnCorrectStats() {
        Expense e1 = Expense.builder().amount(new BigDecimal("100000")).category(category).user(user).build();
        Expense e2 = Expense.builder().amount(new BigDecimal("200000")).category(category).user(user).build();

        when(expenseRepo.findByUser(user)).thenReturn(List.of(e1, e2));

        List<CategoryExpenseStats> stats = expenseService.getStatsByCategory(user);

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getCategory()).isEqualTo("Food");
        assertThat(stats.get(0).getTotal()).isEqualTo(new BigDecimal("300000"));
    }

}