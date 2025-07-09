package com.nvd.expensetracker.controller;

import com.nvd.expensetracker.dto.BudgetRequest;
import com.nvd.expensetracker.dto.BudgetResponse;
import com.nvd.expensetracker.dto.ExpenseResponse;
import com.nvd.expensetracker.exception.AccessDeniedException;
import com.nvd.expensetracker.exception.ResourceNotFoundException;
import com.nvd.expensetracker.model.Budget;
import com.nvd.expensetracker.model.Category;
import com.nvd.expensetracker.model.Expense;
import com.nvd.expensetracker.model.User;
import com.nvd.expensetracker.repository.BudgetRepository;
import com.nvd.expensetracker.repository.CategoryRepository;
import com.nvd.expensetracker.repository.ExpenseRepository;
import com.nvd.expensetracker.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final ModelMapper modelMapper;
    private final BudgetRepository budgetRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;

    @GetMapping
    public ResponseEntity<?> getAllBudgets(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow();
        List<BudgetResponse> response = budgetRepo.findByUser(user).stream()
                .map(b -> BudgetResponse.builder()
                        .id(b.getId())
                        .amount(b.getAmount())
                        .categoryName(b.getCategory().getName())
                        .build())
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createBudget(
            @Valid @RequestBody BudgetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow();
        Category category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Budget budget = Budget.builder()
                .amount(request.getAmount())
                .category(category)
                .user(user)
                .build();
        Budget saved = budgetRepo.save(budget);
        BudgetResponse response = modelMapper.map(saved, BudgetResponse.class);
        response.setCategoryName(category.getName());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow();

        return budgetRepo.findById(id).map(budget -> {
            if (!budget.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("You do not have permission to update this budget.");
            }

            Category category = categoryRepo.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

            budget.setAmount(request.getAmount());
            budget.setCategory(category);
            Budget savedBudget = budgetRepo.save(budget);

            BudgetResponse budgetResponse = BudgetResponse.builder()
                    .id(savedBudget.getId())
                    .amount(savedBudget.getAmount())
                    .categoryName(category.getName())
                    .build();
            return ResponseEntity.ok(budgetResponse);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow();
        return budgetRepo.findById(id).map(bud -> {
            if (!bud.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("You do not have permission to delete it");
            }
            budgetRepo.deleteById(id);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
