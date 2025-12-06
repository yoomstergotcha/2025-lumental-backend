package com.example.lumental.controller;

import com.example.lumental.dto.ApiResponse;
import com.example.lumental.dto.UserOnboardingRequest;
import com.example.lumental.dto.UserOnboardingResponse;
import com.example.lumental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/onboarding")
    public ApiResponse<UserOnboardingResponse> onboarding(@RequestBody UserOnboardingRequest req) {

        if (req.getUsername() == null || req.getUsername().isBlank()) {
            return ApiResponse.error("username is required");
        }

        UserOnboardingResponse created = userService.createUser(req);
        return ApiResponse.success(created);
    }
}
