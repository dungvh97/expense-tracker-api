package com.nvd.expensetracker.service;

import com.nvd.expensetracker.model.Expense;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Service
public class ExcelExportService {

    public void writeExpensesToExcel(List<Expense> expenses, OutputStream out) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Expenses");

        // Header row
        Row header = sheet.createRow(0);
        String[] columns = { "ID", "Amount", "Description", "Date", "Category" };
        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
        }

        // Data row
        int rowNum = 1;
        for (Expense exp : expenses) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(exp.getId());
            row.createCell(1).setCellValue(exp.getAmount().doubleValue());
            row.createCell(2).setCellValue(exp.getDescription());
            row.createCell(3).setCellValue(exp.getDate().toString());
            row.createCell(4).setCellValue(exp.getCategory().getName());
        }

        workbook.write(out);
        workbook.close();
    }
}
