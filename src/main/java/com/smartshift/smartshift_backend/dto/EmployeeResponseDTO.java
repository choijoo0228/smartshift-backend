package com.smartshift.smartshift_backend.dto;

import com.smartshift.smartshift_backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class EmployeeResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Role role;
    private boolean hasLogin;
    private String loginStatus;
}