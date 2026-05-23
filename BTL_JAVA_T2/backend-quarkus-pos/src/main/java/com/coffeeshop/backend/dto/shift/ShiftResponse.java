package com.coffeeshop.backend.dto.shift;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShiftResponse {
    private Long id;
    private String cashierName;
    private String startTime;
    private String endTime;
    private Long startingCash;
    private Long difference;
    private String status;
}
