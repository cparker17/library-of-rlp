package com.parker.rlp.services;

import com.parker.rlp.models.User;
import org.springframework.stereotype.Service;

@Service
public interface AddressService {
    void resetAddress(User user);
}
