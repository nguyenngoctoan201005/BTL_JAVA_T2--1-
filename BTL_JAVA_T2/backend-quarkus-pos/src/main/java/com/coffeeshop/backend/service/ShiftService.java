package com.coffeeshop.backend.service;

import com.coffeeshop.backend.dto.shift.CloseShiftRequest;
import com.coffeeshop.backend.dto.shift.OpenShiftRequest;
import com.coffeeshop.backend.dto.shift.ShiftResponse;

import java.util.List;

public interface ShiftService {
    List<ShiftResponse> getAllShifts();
    void openShift(OpenShiftRequest request);
    void closeShift(Long id, CloseShiftRequest request);
}
