package com.example.lumental.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class ChatAnalysisRequest {

    private Long userId;
    private String userMessage;

    private Map<String, Object> bio_context;
}
