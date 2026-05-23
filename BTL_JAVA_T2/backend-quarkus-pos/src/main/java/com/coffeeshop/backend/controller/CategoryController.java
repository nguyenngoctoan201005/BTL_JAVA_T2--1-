package com.coffeeshop.backend.controller;

import com.coffeeshop.backend.dto.category.CategoryDTO;
import com.coffeeshop.backend.dto.category.CreateCategoryRequest;
import com.coffeeshop.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/all")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CreateCategoryRequest request) {
        CategoryDTO createdCategory = categoryService.createCategory(request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }
}
