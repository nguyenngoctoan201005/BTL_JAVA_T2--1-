package com.coffeeshop.backend.dto.user;

import lombok.Data;

@Data
public class UserSummary {
    private Long id;
    private String fullname;
    private String phone;
    private String address;
}
