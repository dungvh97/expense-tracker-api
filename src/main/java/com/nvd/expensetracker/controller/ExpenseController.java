package com.nvd.expensetracker.controller;

import com.nvd.expensetracker.dto.CategoryExpenseStats;
import com.nvd.expensetracker.dto.ExpenseRequest;
import com.nvd.expensetracker.dto.ExpenseResponse;
import com.nvd.expensetracker.dto.MonthlyStats;
import com.nvd.expensetracker.exception.AccessDeniedException;
import com.nvd.expensetracker.exception.ResourceNotFoundException;
import com.nvd.expensetracker.logging.LogUtil;
import com.nvd.expensetracker.model.Category;
import com.nvd.expensetracker.model.Expense;
import com.nvd.expensetracker.model.User;
import com.nvd.expensetracker.repository.CategoryRepository;
import com.nvd.expensetracker.repository.ExpenseRepository;
import com.nvd.expensetracker.repository.UserRepository;
import com.nvd.expensetracker.service.CsvExportService;
import com.nvd.expensetracker.service.ExcelExportService;
import com.nvd.expensetracker.service.ExpenseService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ModelMapper modelMapper;
    private final ExpenseRepository expenseRepo;
    private final ExpenseService expenseService;
    private final CsvExportService csvExportService;
    private final ExcelExportService excelExportService;
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
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

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

    @GetMapping("/stats/monthly")
    public ResponseEntity<List<MonthlyStats>> getStatsByMonth(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow();
        List<MonthlyStats> stats = expenseService.getMonthlyStats(user);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/export/csv")
    public void exportExpensesToCsv(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response) throws IOException {

        User user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow();
        List<Expense> expenses = expenseRepo.findByUser(user);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=expenses.csv");

        // Write BOM UTF-8 first
        OutputStream out = response.getOutputStream();
        out.write(0xEF); out.write(0xBB); out.write(0xBF); // BOM: UTF-8

        PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8);
        csvExportService.writeExpensesToCsv(expenses, writer);
    }

    @GetMapping("/export/excel")
    public void exportExpensesToExcel(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response) throws IOException {

        User user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow();
        List<Expense> expenses = expenseRepo.findByUser(user);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=expenses.xlsx");

        excelExportService.writeExpensesToExcel(expenses, response.getOutputStream());
    }
}