package com.smartserve.repository;

import com.smartserve.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByIsActiveTrueOrderByName();
    boolean existsByNameIgnoreCase(String name);
}
