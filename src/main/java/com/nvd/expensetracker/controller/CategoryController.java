package com.nvd.expensetracker.controller;

import com.nvd.expensetracker.dto.CategoryRequest;
import com.nvd.expensetracker.dto.CategoryResponse;
import com.nvd.expensetracker.exception.ResourceNotFoundException;
import com.nvd.expensetracker.model.Category;
import com.nvd.expensetracker.repository.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepo;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryRepo.findAll().stream()
                .map(c -> CategoryResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                .toList();

        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .build();
        Category saved = categoryRepo.save(category);
        return ResponseEntity.ok(modelMapper.map(saved, CategoryResponse.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        Category category = categoryRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setName(request.getName());

        Category saved = categoryRepo.save(category);
        return ResponseEntity.ok(modelMapper.map(saved, CategoryResponse.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!categoryRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        categoryRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
