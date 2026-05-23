package com.coffeeshop.backend.repository;

import com.coffeeshop.backend.entity.Shift;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class ShiftRepository implements PanacheRepository<Shift> {
    
    public Optional<Shift> findOpenShift() {
        return find("status", "OPEN").firstResultOptional();
    }
}
