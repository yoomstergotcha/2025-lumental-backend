package com.example.lumental.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String externalId;
    private String nickname;
    private OffsetDateTime createdAt;
}
