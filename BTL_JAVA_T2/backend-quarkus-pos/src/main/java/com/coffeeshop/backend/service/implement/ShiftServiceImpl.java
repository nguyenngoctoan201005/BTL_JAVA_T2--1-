package com.coffeeshop.backend.service.implement;

import com.coffeeshop.backend.dto.shift.CloseShiftRequest;
import com.coffeeshop.backend.dto.shift.OpenShiftRequest;
import com.coffeeshop.backend.dto.shift.ShiftResponse;
import com.coffeeshop.backend.entity.Shift;
import com.coffeeshop.backend.repository.ShiftRepository;
import com.coffeeshop.backend.service.ShiftService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import io.quarkus.panache.common.Sort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class ShiftServiceImpl implements ShiftService {

    @Inject
    ShiftRepository shiftRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<ShiftResponse> getAllShifts() {
        return shiftRepository.findAll(Sort.descending("id")).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void openShift(OpenShiftRequest request) {
        Optional<Shift> openShift = shiftRepository.findOpenShift();
        if (openShift.isPresent()) {
            throw new BadRequestException("Có ca đang mở, không thể mở thêm");
        }

        Shift shift = new Shift();
        shift.setCashierName("Thu Ngân");
        shift.setStartTime(LocalDateTime.now());
        shift.setStartingCash(request.getStartingCash());
        shift.setStatus("OPEN");

        shiftRepository.persist(shift);
    }

    @Override
    @Transactional
    public void closeShift(Long id, CloseShiftRequest request) {
        Shift shift = shiftRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Shift not found"));

        if ("CLOSED".equals(shift.getStatus())) {
            throw new BadRequestException("Ca làm việc này đã đóng");
        }

        shift.setEndTime(LocalDateTime.now());
        shift.setActualCash(request.getActualCash());
        
        long startingCash = shift.getStartingCash() != null ? shift.getStartingCash() : 0L;
        long actualCash = request.getActualCash() != null ? request.getActualCash() : 0L;
        shift.setDifference(actualCash - startingCash);
        
        shift.setStatus("CLOSED");

        shiftRepository.persist(shift);
    }

    private ShiftResponse mapToResponse(Shift shift) {
        ShiftResponse response = new ShiftResponse();
        response.setId(shift.getId());
        response.setCashierName(shift.getCashierName());
        response.setStartTime(shift.getStartTime() != null ? shift.getStartTime().format(formatter) : null);
        response.setEndTime(shift.getEndTime() != null ? shift.getEndTime().format(formatter) : null);
        response.setStartingCash(shift.getStartingCash());
        response.setDifference(shift.getDifference());
        response.setStatus(shift.getStatus());
        return response;
    }
}
