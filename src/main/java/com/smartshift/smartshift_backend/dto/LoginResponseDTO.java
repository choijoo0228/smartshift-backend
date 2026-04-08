package com.smartshift.smartshift_backend.dto;

import com.smartshift.smartshift_backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDTO {
    private Long  id;
    private String username;
    private Role role;
    private Long employeeId;
    private String message;
    private boolean firstLogin;
}
