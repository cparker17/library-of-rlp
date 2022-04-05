package com.parker.rlp.services.impl;

import com.parker.rlp.models.User;
import com.parker.rlp.repositories.AddressRepository;
import com.parker.rlp.services.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    AddressRepository addressRepository;

    @Override
    public void resetAddress(User user) {
        Long addressId = user.getAddress().getId();
        addressRepository.deleteById(addressId);
    }
}
