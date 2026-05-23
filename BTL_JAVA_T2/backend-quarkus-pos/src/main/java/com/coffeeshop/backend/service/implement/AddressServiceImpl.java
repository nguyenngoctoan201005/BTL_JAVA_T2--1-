package com.coffeeshop.backend.service.implement;

import com.coffeeshop.backend.dto.address.AddressDTO;
import com.coffeeshop.backend.entity.Address;
import com.coffeeshop.backend.entity.User;
import com.coffeeshop.backend.exception.ResourceNotFoundException;
import com.coffeeshop.backend.mapper.AddressMapper;
import com.coffeeshop.backend.repository.AddressRepository;
import com.coffeeshop.backend.repository.UserRepository;
import com.coffeeshop.backend.service.AddressService;
import lombok.RequiredArgsConstructor;
import java.lang.SecurityException;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public List<AddressDTO> getUserAddresses(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        return addressRepository.findByUserId(user.getId()).stream()
                .map(addressMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        Address address = addressMapper.toEntity(addressDTO);
        address.setUser(user);

        if (address.isDefault()) {
            // Thay thế vòng lặp forEach bằng 1 query duy nhất
            addressRepository.resetDefaultAddressForUser(user.getId());
        }
        Address savedAddress = addressRepository.save(address);
        return addressMapper.toDTO(savedAddress);
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You are not authorized to update this address.");
        }

        address.setAddressText(addressDTO.getAddressText());
        address.setLabel(addressDTO.getLabel());
        address.setLatitude(addressDTO.getLatitude());
        address.setLongitude(addressDTO.getLongitude());
        address.setNotes(addressDTO.getNotes());

        if (addressDTO.isDefault()) {
            addressRepository.findByUserId(user.getId()).forEach(addr -> addr.setDefault(false));
            address.setDefault(true);
        } else {
            address.setDefault(false);
        }

        Address updatedAddress = addressRepository.save(address);
        return addressMapper.toDTO(updatedAddress);
    }

    @Override
    public void deleteAddress(Long addressId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You are not authorized to delete this address.");
        }

        addressRepository.delete(address);
    }

    @Override
    public AddressDTO setDefaultAddress(Long addressId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        Address addressToSetDefault = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        if (!addressToSetDefault.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You are not authorized to modify this address.");
        }

        addressRepository.findByUserId(user.getId()).forEach(addr -> addr.setDefault(false));
        addressToSetDefault.setDefault(true);
        
        addressRepository.save(addressToSetDefault);
        return addressMapper.toDTO(addressToSetDefault);
    }

    @Override
    @Transactional
    public AddressDTO getDefaultAddress(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        return addressRepository.findByUserId(user.getId()).stream()
                .filter(Address::isDefault)
                .findFirst()
                .map(addressMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No default address found for user: " + userEmail));
    }

    @Override
    @Transactional
    public AddressDTO getAddressById(Long addressId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You are not authorized to view this address.");
        }

        return addressMapper.toDTO(address);
    }
}
