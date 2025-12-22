package com.example.backend.mapper;

import com.example.backend.dto.EmployeeResponse;
import com.example.backend.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public EmployeeResponse toResponse(Employee employee) {
        if (employee == null) {
            return null;
        }
        return new EmployeeResponse(employee.getId(), employee.getName(), employee.getEmail());
    }
}
