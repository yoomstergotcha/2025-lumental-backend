package com.example.lumental.controller;

import com.example.lumental.dto.ApiResponse;
import com.example.lumental.dto.ChatMessageRequest;
import com.example.lumental.dto.ChatResponseDto;
import com.example.lumental.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/message")
    public ApiResponse<ChatResponseDto> sendMessage(@RequestBody ChatMessageRequest req) {

        if (req.getUserId() == null) {
            return ApiResponse.error("userId is required");
        }
        if (req.getMessage() == null || req.getMessage().isBlank()) {
            return ApiResponse.error("message is required");
        }

        ChatResponseDto response = chatService.handleChat(req);
        return ApiResponse.success(response);
    }
}
