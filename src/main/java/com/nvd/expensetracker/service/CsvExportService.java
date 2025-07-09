package com.nvd.expensetracker.service;

import com.nvd.expensetracker.model.Expense;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.util.List;

@Service
public class CsvExportService {

    public void writeExpensesToCsv(List<Expense> expenses, PrintWriter writer) {
        writer.println("ID,Amount,Description,Date,Category");
        for (Expense expense : expenses) {
            writer.printf("%d,%.2f,%s,%s,%s%n",
                    expense.getId(),
                    expense.getAmount(),
                    expense.getDescription().replace(",", ""),
                    expense.getDate(),
                    expense.getCategory().getName()
            );
        }
    }
}
