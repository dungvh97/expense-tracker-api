package com.nvd.expensetracker.controller;

import com.nvd.expensetracker.dto.CategoryExpenseStats;
import com.nvd.expensetracker.dto.ExpenseRequest;
import com.nvd.expensetracker.dto.ExpenseResponse;
import com.nvd.expensetracker.exception.AccessDeniedException;
import com.nvd.expensetracker.model.*;
import com.nvd.expensetracker.repository.*;
import com.nvd.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ModelMapper modelMapper;
    private final ExpenseRepository expenseRepo;
    private final ExpenseService expenseService;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;

    @GetMapping
    public ResponseEntity<?> getAllExpenses(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow();

        List<ExpenseResponse> response = expenseRepo.findByUser(user).stream()
                .map(exp -> ExpenseResponse.builder()
                        .id(exp.getId())
                        .description(exp.getDescription())
                        .amount(exp.getAmount())
                        .date(exp.getDate())
                        .categoryName(exp.getCategory().getName())
                        .build())
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow();
        Category category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Expense expense = Expense.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .date(request.getDate())
                .user(user)
                .category(category)
                .build();

        Expense saved = expenseRepo.save(expense);

        ExpenseResponse response = modelMapper.map(saved, ExpenseResponse.class);
        response.setCategoryName(category.getName());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow();
        Category category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return expenseRepo.findById(id).map(exp -> {
            if (!exp.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("You do not have permission to update this expense.");
            }

            exp.setAmount(request.getAmount());
            exp.setDescription(request.getDescription());
            exp.setDate(request.getDate());
            exp.setCategory(category);

            expenseRepo.save(exp);

            ExpenseResponse response = ExpenseResponse.builder()
                    .id(exp.getId())
                    .description(exp.getDescription())
                    .amount(exp.getAmount())
                    .date(exp.getDate())
                    .categoryName(category.getName())
                    .build();

            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow();
        return expenseRepo.findById(id).map(exp -> {
            if (!exp.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("You do not have permission to delete it");
            }
            expenseRepo.deleteById(id);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filterExpenses(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow();
        List<Expense> filtered = expenseService.filterExpenses(user, categoryId, startDate, endDate);
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/total")
    public ResponseEntity<?> getTotalAmount(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow();
        BigDecimal totalExpense = expenseService.getTotalExpense(user, startDate, endDate);
        return ResponseEntity.ok(Map.of("total", totalExpense));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getCategoryStats(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow();
        List<CategoryExpenseStats> stats = expenseService.getStatsByCategory(user);
        return ResponseEntity.ok(Map.of("total", stats));
    }

    @GetMapping("/stats/filter")
    public ResponseEntity<?> getFilteredStats(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow();
        BigDecimal total = expenseService.getFilteredTotal(user, categoryId, startDate, endDate);
        return ResponseEntity.ok(Map.of("total", total));
    }
}
