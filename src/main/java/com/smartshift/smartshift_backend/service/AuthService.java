package com.smartshift.smartshift_backend.service;

import com.smartshift.smartshift_backend.dto.ChangePasswordRequestDTO;
import com.smartshift.smartshift_backend.dto.CreateUserRequestDTO;
import com.smartshift.smartshift_backend.dto.LoginRequestDTO;
import com.smartshift.smartshift_backend.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);

    void changePassword(ChangePasswordRequestDTO request);

    void createUserFromEmployee(CreateUserRequestDTO request);
}