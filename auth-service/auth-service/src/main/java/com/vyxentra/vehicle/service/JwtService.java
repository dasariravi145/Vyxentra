package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String userId, String mobileNumber, Set<UserRole> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        List<String> roleStrings = roles.stream()
                .map(UserRole::name)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(userId)
                .claim("mobileNumber", mobileNumber)
                .claim("roles", roleStrings)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String userId, String mobileNumber) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .setSubject(userId)
                .claim("mobileNumber", mobileNumber)
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractMobileNumber(String token) {
        return extractAllClaims(token).get("mobileNumber", String.class);
    }

    public Set<UserRole> extractRoles(String token) {
        List<String> roles = extractAllClaims(token).get("roles", List.class);
        return roles.stream()
                .map(UserRole::valueOf)
                .collect(Collectors.toSet());
    }

    public Date getExpirationDate(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public Long getExpirationTime() {
        return expiration;
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }
}

