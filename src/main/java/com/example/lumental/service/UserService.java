package com.example.lumental.service;

import com.example.lumental.domain.User;
import com.example.lumental.dto.UserOnboardingRequest;
import com.example.lumental.dto.UserOnboardingResponse;
import com.example.lumental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserOnboardingResponse createUser(UserOnboardingRequest req) {

        if (req.getUsername() == null || req.getUsername().isBlank()) {
            throw new IllegalArgumentException("username is required");
        }

        User user = User.builder()
                .nickname(req.getUsername())

                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        return UserOnboardingResponse.builder()
                .userId(user.getId())
                .username(user.getNickname())
                .createdAt(user.getCreatedAt().toString())
                .build();
    }
}
