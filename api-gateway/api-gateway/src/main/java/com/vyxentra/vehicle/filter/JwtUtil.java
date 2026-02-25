package com.vyxentra.vehicle.filter;

import com.vyxentra.vehicle.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    public Set<UserRole> extractUserRoles(String token) {
        List<String> roleStrings = extractRoles(token);
        return roleStrings.stream()
                .map(UserRole::valueOf)
                .collect(Collectors.toSet());
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            boolean isValid = !isTokenExpired(token);

            if (isValid) {
                log.debug("Token validated successfully for user: {}", claims.getSubject());
            } else {
                log.warn("Token expired for user: {}", claims.getSubject());
            }

            return isValid;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean hasRole(String token, UserRole role) {
        Set<UserRole> roles = extractUserRoles(token);
        return roles.contains(role);
    }

    public boolean hasAnyRole(String token, Set<UserRole> requiredRoles) {
        Set<UserRole> userRoles = extractUserRoles(token);
        return userRoles.stream().anyMatch(requiredRoles::contains);
    }
}
