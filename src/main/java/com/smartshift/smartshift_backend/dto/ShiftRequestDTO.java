package com.smartshift.smartshift_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.smartshift.smartshift_backend.entity.Employee;
import com.smartshift.smartshift_backend.entity.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ShiftRequestDTO {
    private Long id;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Role roleRequired;
    private Employee assignedEmployee;
    private boolean published = false;
}
