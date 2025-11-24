package com.example.lumental.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailySummaryResponseDto {

    private String mood;
    private double moodConfidence;

    private HeartRateBlock heartRate;
    private HrvBlock hrv;
    private SleepBlock sleep;

    @Getter @Setter
    @Builder @NoArgsConstructor @AllArgsConstructor
    public static class HeartRateBlock {
        private double min;
        private double max;
        private List<TimeValue> data;
    }

    @Getter @Setter
    @Builder @NoArgsConstructor @AllArgsConstructor
    public static class HrvBlock {
        private double avg;
        private List<TimeValue> data;
    }

    @Getter @Setter
    @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SleepBlock {
        private int totalMinutes;
        private int deepMinutes;
        private int lightMinutes;
        private int remMinutes;
        private List<SleepSegment> segments;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TimeValue {
        private String ts;
        private double value;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SleepSegment {
        private String start;
        private String end;
        private String stage; // "light", "deep", "rem", "awake"
    }
}

