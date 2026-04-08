package com.smartshift.smartshift_backend.service;

import com.smartshift.smartshift_backend.entity.Employee;
import com.smartshift.smartshift_backend.dto.EmployeeResponseDTO;

import java.util.List;

public interface EmployeeService {

    List<EmployeeResponseDTO> getAllEmployees();

    Employee getEmployeeById(Long id);

    Employee createEmployee(Employee employee);

    Employee updateEmployee(Long id, Employee employee);

    void deleteEmployee(Long id);
}