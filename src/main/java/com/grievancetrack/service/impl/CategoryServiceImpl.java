package com.grievancetrack.service.impl;

import com.grievancetrack.dto.request.CategoryRequest;
import com.grievancetrack.dto.response.CategoryResponse;
import com.grievancetrack.entity.Category;
import com.grievancetrack.entity.Department;
import com.grievancetrack.exception.InvalidRequestException;
import com.grievancetrack.exception.ResourceNotFoundException;
import com.grievancetrack.repository.CategoryRepository;
import com.grievancetrack.repository.DepartmentRepository;
import com.grievancetrack.repository.GrievanceRepository;
import com.grievancetrack.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;
    private final GrievanceRepository grievanceRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               DepartmentRepository departmentRepository,
                               GrievanceRepository grievanceRepository) {
        this.categoryRepository = categoryRepository;
        this.departmentRepository = departmentRepository;
        this.grievanceRepository = grievanceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return CategoryResponse.fromEntity(cat);
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new InvalidRequestException("Category with name '" + request.getName() + "' already exists");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        Category category = new Category(request.getName().trim(), request.getSlaDays(), department);
        Category saved = categoryRepository.save(category);
        return CategoryResponse.fromEntity(saved);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.getName().trim(), id)) {
            throw new InvalidRequestException("Another category with name '" + request.getName() + "' already exists");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        category.setName(request.getName().trim());
        category.setSlaDays(request.getSlaDays());
        category.setDepartment(department);

        Category updated = categoryRepository.save(category);
        return CategoryResponse.fromEntity(updated);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        long grievanceCount = grievanceRepository.countByCategoryId(id);
        if (grievanceCount > 0) {
            throw new InvalidRequestException("Cannot delete category with " + grievanceCount + " linked grievances");
        }

        categoryRepository.delete(category);
    }
}
