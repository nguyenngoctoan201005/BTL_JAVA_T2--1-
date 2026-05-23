package com.coffeeshop.backend.controller;

import com.coffeeshop.backend.dto.address.AddressDTO;
import com.coffeeshop.backend.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // TODO: Replace hardcoded email with Quarkus SecurityIdentity in all methods
    private static final String STUB_EMAIL = "admin@gmail.com";

    @GetMapping
    public ResponseEntity<List<AddressDTO>> getUserAddresses() {
        List<AddressDTO> addresses = addressService.getUserAddresses(STUB_EMAIL);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long id) {
        AddressDTO address = addressService.getAddressById(id, STUB_EMAIL);
        return ResponseEntity.ok(address);
    }

    @PostMapping
    public ResponseEntity<AddressDTO> createAddress(@RequestBody AddressDTO addressDTO) {
        AddressDTO createdAddress = addressService.createAddress(addressDTO, STUB_EMAIL);
        return new ResponseEntity<>(createdAddress, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long id, @RequestBody AddressDTO addressDTO) {
        AddressDTO updatedAddress = addressService.updateAddress(id, addressDTO, STUB_EMAIL);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id, STUB_EMAIL);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<AddressDTO> setDefaultAddress(@PathVariable Long id) {
        AddressDTO defaultAddress = addressService.setDefaultAddress(id, STUB_EMAIL);
        return ResponseEntity.ok(defaultAddress);
    }

    @GetMapping("/default")
    public ResponseEntity<AddressDTO> getDefaultAddress() {
        AddressDTO defaultAddress = addressService.getDefaultAddress(STUB_EMAIL);
        return ResponseEntity.ok(defaultAddress);
    }
}
