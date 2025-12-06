package com.example.lumental.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChatMessageRequest {
    private Long userId;
    private String message;
}
