package com.smartshift.smartshift_backend.controller;

import com.smartshift.smartshift_backend.dto.ShiftRequestDTO;
import com.smartshift.smartshift_backend.entity.Shift;
import com.smartshift.smartshift_backend.service.ShiftService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shifts")
@CrossOrigin(origins = "*")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;;
    }

    @GetMapping
    public List<Shift> getAllShifts() {
        return shiftService.getAllShifts();
    }

    @GetMapping("/{id}")
    public Shift getShiftById(@PathVariable Long id) {
        return shiftService.getShiftById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Shift createShift(@RequestBody ShiftRequestDTO shiftDTO) {
        System.out.println("::::::>>>>>>" + shiftDTO.isPublished());
        return shiftService.createShift(shiftDTO);
    }

    @PutMapping("/{id}")
    public Shift updateShift(@PathVariable Long id, @RequestBody Shift updatedShift) {
        return shiftService.updateShift(id, updatedShift);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShiftById(@PathVariable Long id) {
        shiftService.deleteShift(id);
    }

    @GetMapping("/date/{shiftDate}")
    public List<Shift> getShiftsByDate(@PathVariable LocalDate shiftDate) {
        return shiftService.getShiftsByDate(shiftDate);
    }

    @GetMapping("/week")
    public List<Shift> getShiftsByBetweenDates(@RequestParam LocalDate startDate,@RequestParam LocalDate endDate) {
        return shiftService.getShiftsForWeek(startDate, endDate);
    }

    @PostMapping("/publish-week")
    public void publishWeek(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        shiftService.publishWeek(startDate, endDate);
    }

    @GetMapping("/published/week")
    public List<Shift> getPublishedShiftsForWeek(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return shiftService.getPublishedShiftsForWeek(startDate, endDate);
    }

}
