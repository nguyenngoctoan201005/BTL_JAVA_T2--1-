package com.coffeeshop.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "shifts")
@Getter
@Setter
public class Shift extends BaseEntity {

    @Column(name = "cashier_name")
    private String cashierName;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "starting_cash", nullable = false)
    private Long startingCash;

    @Column(name = "actual_cash")
    private Long actualCash;

    @Column(name = "difference")
    private Long difference;

    @Column(name = "status", nullable = false)
    private String status;
}
