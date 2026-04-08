package com.smartshift.smartshift_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequestDTO {
    private Long userId;
    private String newPassword;
}
