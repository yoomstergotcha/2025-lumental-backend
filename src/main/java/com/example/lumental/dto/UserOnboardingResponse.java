package com.example.lumental.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserOnboardingResponse {
    private Long userId;
    private String username;
    private String createdAt;
}
