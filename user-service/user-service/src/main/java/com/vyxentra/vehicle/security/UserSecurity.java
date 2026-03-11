package com.vyxentra.vehicle.security;

import com.vyxentra.vehicle.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {

    private final UserService userService;

    public boolean isCurrentUser(String userId) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        return currentUserId.equals(userId);
    }
}
