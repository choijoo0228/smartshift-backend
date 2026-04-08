package com.smartshift.smartshift_backend.service.impl;

import com.smartshift.rosterenigne.ShiftTimeValidator;
import com.smartshift.rosterenigne.ShiftConflictChecker;
import com.smartshift.smartshift_backend.dto.ShiftRequestDTO;
import com.smartshift.smartshift_backend.entity.Employee;
import com.smartshift.smartshift_backend.entity.Shift;
import com.smartshift.smartshift_backend.repository.EmployeeRepository;
import com.smartshift.smartshift_backend.repository.ShiftRepository;
import com.smartshift.smartshift_backend.service.EmployeeService;
import com.smartshift.smartshift_backend.service.NotificationService;
import com.smartshift.smartshift_backend.service.ShiftService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;

    private final ShiftConflictChecker shiftConflictChecker = new ShiftConflictChecker();
    private final ShiftTimeValidator shiftTimeValidator = new ShiftTimeValidator();
    private final NotificationService notificationService = new NotificationServiceImpl();

    public ShiftServiceImpl(ShiftRepository shiftRepository, EmployeeRepository employeeRepository) {
        this.shiftRepository = shiftRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    @Override
    public Shift getShiftById(Long id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Shift not found with id: " + id
                ));
    }

    @Override
    public Shift createShift(ShiftRequestDTO shiftDTO) {
        Shift shift = new Shift();
        shift.setShiftDate(shiftDTO.getShiftDate());
        shift.setPublished(shiftDTO.isPublished());
        shift.setAssignedEmployee(shiftDTO.getAssignedEmployee());
        shift.setStartTime(shiftDTO.getStartTime());
        shift.setEndTime(shiftDTO.getEndTime());
        shift.setRoleRequired(shiftDTO.getRoleRequired());
        validateShift(shift, null);
        return shiftRepository.save(shift);
    }

    @Override
    public Shift updateShift(Long id, Shift shift) {
        Shift existingShift = getShiftById(id);

        System.out.println("::::::>>>>>>" + shift.getAssignedEmployee().getId());
        System.out.println("::::::>>>>>>" + existingShift.getAssignedEmployee().getId());

        existingShift.setShiftDate(shift.getShiftDate());
        existingShift.setStartTime(shift.getStartTime());
        existingShift.setEndTime(shift.getEndTime());
        existingShift.setRoleRequired(shift.getRoleRequired());
        existingShift.setAssignedEmployee(shift.getAssignedEmployee());

        validateShift(existingShift, id);
        return shiftRepository.save(existingShift);
    }

    @Override
    public void deleteShift(Long id) {
        Shift existingShift = getShiftById(id);
        shiftRepository.delete(existingShift);
    }

    @Override
    public List<Shift> getShiftsByDate(LocalDate shiftDate) {
        return shiftRepository.findByShiftDate(shiftDate);
    }

    @Override
    public List<Shift> getShiftsForWeek(LocalDate start, LocalDate end) {
        return shiftRepository.findByShiftDateBetween(start, end);
    }

    @Override
    public void publishWeek(LocalDate start, LocalDate end) {
        List<Shift> shifts = shiftRepository.findByShiftDateBetween(start, end);

        if (shifts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No shifts found for the selected week");
        }

        boolean allAlreadyPublished = shifts.stream().allMatch(Shift::isPublished);

        if(allAlreadyPublished) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All shifts for this week are already published");
        }

        for (Shift shift : shifts) {
            shift.setPublished(true);
            shiftRepository.save(shift);

            if (shift.getAssignedEmployee() != null && shift.getAssignedEmployee().getEmail() != null) {
                try {
                    notificationService.sendShiftPublishedEmail(shift);
                } catch (Exception e) {
                    System.out.println("SES send failed: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public List<Shift> getPublishedShiftsForWeek(LocalDate start, LocalDate end) {
        return shiftRepository.findByShiftDateBetweenAndPublishedTrue(start, end);
    }

    private void validateShift(Shift shift, Long currentShiftId) {
        if (!shiftTimeValidator.isValidShiftTime(shift.getStartTime(), shift.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "shift start time must be before end time"
            );
        }

        if (shift.getAssignedEmployee() == null || shift.getAssignedEmployee().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Assigned employee is required"
            );
        }


        Long employeeId = shift.getAssignedEmployee().getId();

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee with id " + employeeId + " not found"
                ));

        List<Shift> existingShifts =
                shiftRepository.findByAssignedEmployeeIdAndShiftDate(employeeId, shift.getShiftDate());

        for (Shift existing : existingShifts) {
            if (currentShiftId != null && existing.getId().equals(currentShiftId)) {
                continue;
            }

            boolean overlap = shiftConflictChecker.hasOverLap(
                    shift.getStartTime(),
                    shift.getEndTime(),
                    existing.getStartTime(),
                    existing.getEndTime()
            );

            if (overlap) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Employee already has a shift during this time"
                );
            }
        }

        shift.setAssignedEmployee(employee);

    }
}