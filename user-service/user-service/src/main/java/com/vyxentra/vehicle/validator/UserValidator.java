package com.vyxentra.vehicle.validator;


import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    public void validateEmailUniqueness(String email, String userId) {
        if (userRepository.existsByEmail(email)) {
            userRepository.findByEmail(email).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(userId)) {
                    throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "Email already registered");
                }
            });
        }
    }

    public void validatePhoneUniqueness(String phoneNumber, String userId) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            userRepository.findByPhoneNumber(phoneNumber).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(userId)) {
                    throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "Phone number already registered");
                }
            });
        }
    }
}
