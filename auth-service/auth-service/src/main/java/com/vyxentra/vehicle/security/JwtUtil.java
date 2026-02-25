package com.vyxentra.vehicle.security;

import com.vyxentra.vehicle.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }
        return null;
    }

    public Set<UserRole> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(authority -> UserRole.valueOf(authority.getAuthority().substring(5)))
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }

    public boolean hasRole(UserRole role) {
        return getCurrentUserRoles().contains(role);
    }

    public boolean hasAnyRole(Set<UserRole> roles) {
        return getCurrentUserRoles().stream().anyMatch(roles::contains);
    }

    public boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }
}
