package com.smartshift.smartshift_backend.service.impl;

import com.smartshift.rosterenigne.ShiftTimeValidator;
import com.smartshift.rosterenigne.ShiftConflictChecker;
import com.smartshift.smartshift_backend.dto.ShiftRequestDTO;
import com.smartshift.smartshift_backend.entity.Employee;
import com.smartshift.smartshift_backend.entity.Shift;
import com.smartshift.smartshift_backend.repository.EmployeeRepository;
import com.smartshift.smartshift_backend.repository.ShiftRepository;
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

    // custom library logic (roster-engine)
    private final ShiftConflictChecker shiftConflictChecker = new ShiftConflictChecker();
    private final ShiftTimeValidator shiftTimeValidator = new ShiftTimeValidator();

    // notification service (mock or SES depending on setup)
    private final NotificationService notificationService = new NotificationServiceImpl();

    public ShiftServiceImpl(ShiftRepository shiftRepository, EmployeeRepository employeeRepository) {
        this.shiftRepository = shiftRepository;
        this.employeeRepository = employeeRepository;
    }

    // get all shifts (used mainly for admin/manager)
    @Override
    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    // get one shift by id
    @Override
    public Shift getShiftById(Long id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Shift not found with id: " + id
                ));
    }

    // create new shift from DTO
    @Override
    public Shift createShift(ShiftRequestDTO shiftDTO) {
        Shift shift = new Shift();

        // map DTO → entity
        shift.setShiftDate(shiftDTO.getShiftDate());
        shift.setPublished(shiftDTO.isPublished()); // usually false when creating
        shift.setAssignedEmployee(shiftDTO.getAssignedEmployee());
        shift.setStartTime(shiftDTO.getStartTime());
        shift.setEndTime(shiftDTO.getEndTime());
        shift.setRoleRequired(shiftDTO.getRoleRequired());

        // validate before saving
        validateShift(shift, null);

        return shiftRepository.save(shift);
    }

    // update existing shift
    @Override
    public Shift updateShift(Long id, Shift shift) {
        Shift existingShift = getShiftById(id);

        System.out.println("new employee id >>> " + shift.getAssignedEmployee().getId());
        System.out.println("old employee id >>> " + existingShift.getAssignedEmployee().getId());

        // update fields
        existingShift.setShiftDate(shift.getShiftDate());
        existingShift.setStartTime(shift.getStartTime());
        existingShift.setEndTime(shift.getEndTime());
        existingShift.setRoleRequired(shift.getRoleRequired());
        existingShift.setAssignedEmployee(shift.getAssignedEmployee());

        // validate updated shift
        validateShift(existingShift, id);

        return shiftRepository.save(existingShift);
    }

    // delete shift
    @Override
    public void deleteShift(Long id) {
        Shift existingShift = getShiftById(id);
        shiftRepository.delete(existingShift);
    }

    // get shifts for a single day
    @Override
    public List<Shift> getShiftsByDate(LocalDate shiftDate) {
        return shiftRepository.findByShiftDate(shiftDate);
    }

    // get shifts between two dates (weekly view)
    @Override
    public List<Shift> getShiftsForWeek(LocalDate start, LocalDate end) {
        return shiftRepository.findByShiftDateBetween(start, end);
    }

    // publish all shifts in a selected week
    @Override
    public void publishWeek(LocalDate start, LocalDate end) {
        List<Shift> shifts = shiftRepository.findByShiftDateBetween(start, end);

        // no shifts → nothing to publish
        if (shifts.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No shifts found for the selected week"
            );
        }

        // check if already published
        boolean allAlreadyPublished = shifts.stream().allMatch(Shift::isPublished);

        if (allAlreadyPublished) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "All shifts for this week are already published"
            );
        }

        for (Shift shift : shifts) {
            // mark shift as published
            shift.setPublished(true);
            shiftRepository.save(shift);

            // send notification (if email exists)
            if (shift.getAssignedEmployee() != null &&
                    shift.getAssignedEmployee().getEmail() != null) {

                try {
                    notificationService.sendShiftPublishedEmail(shift);
                } catch (Exception e) {
                    // don't break publish if email fails
                    System.out.println("Notification failed: " + e.getMessage());
                }
            }
        }
    }

    // used by employees → only show published shifts
    @Override
    public List<Shift> getPublishedShiftsForWeek(LocalDate start, LocalDate end) {
        return shiftRepository.findByShiftDateBetweenAndPublishedTrue(start, end);
    }

    // core validation logic before saving/updating shift
    private void validateShift(Shift shift, Long currentShiftId) {

        // check start time < end time
        if (!shiftTimeValidator.isValidShiftTime(
                shift.getStartTime(),
                shift.getEndTime())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "shift start time must be before end time"
            );
        }

        // must have assigned employee
        if (shift.getAssignedEmployee() == null ||
                shift.getAssignedEmployee().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Assigned employee is required"
            );
        }

        Long employeeId = shift.getAssignedEmployee().getId();

        // check employee exists
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Employee with id " + employeeId + " not found"
                ));

        // get existing shifts for that employee on same day
        List<Shift> existingShifts =
                shiftRepository.findByAssignedEmployeeIdAndShiftDate(
                        employeeId,
                        shift.getShiftDate()
                );

        for (Shift existing : existingShifts) {

            // skip same shift when updating
            if (currentShiftId != null &&
                    existing.getId().equals(currentShiftId)) {
                continue;
            }

            // check time overlap using custom library
            boolean overlap = shiftConflictChecker.hasOverLap(
                    shift.getStartTime(),
                    shift.getEndTime(),
                    existing.getStartTime(),
                    existing.getEndTime()
            );

            if (overlap) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Employee already has a shift during this time"
                );
            }
        }

        // replace with managed entity (important for JPA)
        shift.setAssignedEmployee(employee);
    }
}