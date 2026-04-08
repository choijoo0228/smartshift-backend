package com.smartshift.smartshift_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequestDTO {

    private String employeeEmail;
    private String username;
    private String password;

}