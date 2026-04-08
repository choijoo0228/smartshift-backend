package com.smartshift.smartshift_backend.repository;

import com.smartshift.smartshift_backend.entity.Shift;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    List<Shift> findByShiftDate(LocalDate date);

    List<Shift> findByShiftDateBetween(LocalDate shiftDateAfter, LocalDate shiftDateBefore);

    List<Shift> findByAssignedEmployeeIdAndShiftDate(Long employeeId, LocalDate date);

    List<Shift> findByShiftDateBetweenAndPublishedTrue(LocalDate shiftDateAfter, LocalDate shiftDateBefore);

}
