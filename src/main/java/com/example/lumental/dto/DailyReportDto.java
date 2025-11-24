package com.example.lumental.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportDto {

    private String summaryDate;
    private String mood;
    private Double moodConfidence;

    private Map<String, Object> heartRate;   // {min, max, data:[{ts,value}]}
    private Map<String, Object> hrv;
    private Map<String, Object> stepCount;
    private Map<String, Object> sleep;
}
