package com.example.lumental.dto;

import lombok.Data;
import java.util.List;

@Data
public class LlmMetaDto {
    private Double calculatedStressLevel;
    private String dominantEmotion;
    private Double sentimentScore;
    private String summaryAdvice;
    private List<String> detectedRisks;
}