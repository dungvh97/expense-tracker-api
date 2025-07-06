package com.nvd.expensetracker.repository;

import com.nvd.expensetracker.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
