package com.coffeeshop.backend.dto.shift;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenShiftRequest {
    private Long userId;
    private Long startingCash;
}
