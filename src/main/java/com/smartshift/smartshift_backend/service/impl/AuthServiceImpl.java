package com.smartshift.smartshift_backend.service.impl;

import com.smartshift.smartshift_backend.dto.ChangePasswordRequestDTO;
import com.smartshift.smartshift_backend.dto.CreateUserRequestDTO;
import com.smartshift.smartshift_backend.dto.LoginRequestDTO;
import com.smartshift.smartshift_backend.dto.LoginResponseDTO;
import com.smartshift.smartshift_backend.entity.Employee;
import com.smartshift.smartshift_backend.entity.User;
import com.smartshift.smartshift_backend.repository.EmployeeRepository;
import com.smartshift.smartshift_backend.repository.UserRepository;
import com.smartshift.smartshift_backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    // password hashing (BCrypt)
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(UserRepository userRepository, EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    // login logic
    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {

        // find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid username or password"
                ));

        // check password using BCrypt (not plain text)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid username or password"
            );
        }

        // try to get linked employee id (if exists)
        Long employeeId = employeeRepository.findById(user.getId())
                .map(Employee::getId)
                .orElse(null);

        // return login response (used by frontend)
        return new LoginResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                employeeId,
                "Login successful",
                user.isFirstLogin()
        );
    }

    // change password (used after first login)
    @Override
    public void changePassword(ChangePasswordRequestDTO request) {

        // find user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        // basic validation
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password cannot be empty"
            );
        }

        if (request.getNewPassword().length() < 6) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password must be at least 6 characters long"
            );
        }

        if (request.getNewPassword().length() > 20) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password must not exceed 20 characters"
            );
        }

        // hash new password before saving
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // mark first login completed
        user.setFirstLogin(false);

        userRepository.save(user);
    }

    // create user account from existing employee
    @Override
    public void createUserFromEmployee(CreateUserRequestDTO request) {

        // check username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Username already exists"
            );
        }

        // find employee by email
        Employee employee = employeeRepository.findByEmail(request.getEmployeeEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found"
                ));

        // prevent duplicate user for same employee
        if (employee.getUser() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "User already exists for this employee"
            );
        }

        // create new user linked to employee
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // always hash password
                .role(employee.getRole()) // inherit role from employee
                .firstLogin(true) // force password change on first login
                .build();

        userRepository.save(user);

        // link user back to employee
        employee.setUser(user);
        employeeRepository.save(employee);
    }
}