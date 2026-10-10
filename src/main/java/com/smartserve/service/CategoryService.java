package com.smartserve.service;

import com.smartserve.entity.*;
import com.smartserve.exception.*;
import com.smartserve.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    @Autowired private CategoryRepository categoryRepository;

    public List<Category> getAllActive() {
        return categoryRepository.findByIsActiveTrueOrderByName();
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category create(String name, String description) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("A category named '" + name + "' already exists.");
        }
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setIsActive(true);
        return categoryRepository.save(category);
    }

    public Category update(Long id, String name, String description, Boolean isActive) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
        category.setName(name);
        category.setDescription(description);
        if (isActive != null) category.setIsActive(isActive);
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
        category.setIsActive(false);
        categoryRepository.save(category);
    }
}
