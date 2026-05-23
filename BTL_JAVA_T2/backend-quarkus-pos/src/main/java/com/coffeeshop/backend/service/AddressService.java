package com.coffeeshop.backend.service;

import com.coffeeshop.backend.dto.address.AddressDTO;
import java.util.List;

public interface AddressService {
    List<AddressDTO> getUserAddresses(String userEmail);
    AddressDTO createAddress(AddressDTO addressDTO, String userEmail);
    AddressDTO updateAddress(Long addressId, AddressDTO addressDTO, String userEmail);
    void deleteAddress(Long addressId, String userEmail);
    AddressDTO setDefaultAddress(Long addressId, String userEmail);
    AddressDTO getDefaultAddress(String userEmail);
    AddressDTO getAddressById(Long addressId, String userEmail);
}
