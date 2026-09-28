package com.grievancetrack.service.impl;

import com.grievancetrack.dto.request.DepartmentRequest;
import com.grievancetrack.dto.response.DepartmentResponse;
import com.grievancetrack.entity.Department;
import com.grievancetrack.exception.InvalidRequestException;
import com.grievancetrack.exception.ResourceNotFoundException;
import com.grievancetrack.repository.DepartmentRepository;
import com.grievancetrack.repository.GrievanceRepository;
import com.grievancetrack.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final GrievanceRepository grievanceRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, GrievanceRepository grievanceRepository) {
        this.departmentRepository = departmentRepository;
        this.grievanceRepository = grievanceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
        return DepartmentResponse.fromEntity(dept);
    }

    @Override
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new InvalidRequestException("Department with name '" + request.getName() + "' already exists");
        }

        Department department = new Department(
                request.getName().trim(),
                request.getOfficerName().trim(),
                request.getContact().trim()
        );
        Department saved = departmentRepository.save(department);
        return DepartmentResponse.fromEntity(saved);
    }

    @Override
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(request.getName().trim(), id)) {
            throw new InvalidRequestException("Another department with name '" + request.getName() + "' already exists");
        }

        department.setName(request.getName().trim());
        department.setOfficerName(request.getOfficerName().trim());
        department.setContact(request.getContact().trim());

        Department updated = departmentRepository.save(department);
        return DepartmentResponse.fromEntity(updated);
    }

    @Override
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        long grievanceCount = grievanceRepository.countByDepartmentId(id);
        if (grievanceCount > 0) {
            throw new InvalidRequestException("Cannot delete department with " + grievanceCount + " linked grievances");
        }

        if (department.getCategories() != null && !department.getCategories().isEmpty()) {
            throw new InvalidRequestException("Cannot delete department with linked categories. Reassign or delete categories first.");
        }

        departmentRepository.delete(department);
    }
}
