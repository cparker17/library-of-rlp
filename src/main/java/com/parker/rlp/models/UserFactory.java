package com.parker.rlp.models;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {
    public static User createUser(Authentication authentication) {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        return securityUser.getUser();
    }
}
