package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.entity.Category;
import com.ecommerce.ecommerce.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * GET /products/categories
     * Fetch all product categories
     *
     * @return JSON response with status and categories list
     */
    @GetMapping()
    public ResponseEntity<Map<String, Object>> listCategories() {
        List<Category> categories = categoryService.getAllCategories();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("categories", categories);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CreateCategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {

        Category category = categoryService.createCategory(request.getName(), request.getDescription());

        return ResponseEntity.ok(
                new CreateCategoryResponse("success", category.getId())
        );
    }

    // DTOs


    public static class CreateCategoryRequest {
        @NotBlank(message = "Category name is required")
        private String name;
        private String description;

        public CreateCategoryRequest(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }


    public static class CreateCategoryResponse {
        private String status;
        private Long categoryId;

        public CreateCategoryResponse(String status, Long categoryId) {
            this.status = status;
            this.categoryId = categoryId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }
    }
}