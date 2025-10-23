package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.entity.Category;
import com.ecommerce.ecommerce.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Fetch all product categories from the database
     *
     * @return List of Category entities
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Creates a new product category
     * @param name Category name
     * @param description Category description
     * @return Newly created Category entity
     * @throws IllegalArgumentException if category name already exists
     */
    @Transactional
    public Category createCategory(String name, String description) {
        // Check for uniqueness
        categoryRepository.findByName(name)
                .ifPresent(c -> { throw new IllegalArgumentException("Category name already exists"); });

        Category category = new Category();
        category.setName(name);
        category.setDescription(description);

        return categoryRepository.save(category);
    }
}