package com.smartserve.controller;

import com.smartserve.dto.response.ApiResponse;
import com.smartserve.entity.Category;
import com.smartserve.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getActiveCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllActive()));
    }
}
