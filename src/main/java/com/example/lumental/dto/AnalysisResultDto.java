// FastAPI → Spring
package com.example.lumental.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class AnalysisResultDto {

    private String summaryDate;
    private Long userId;

    private String mood;
    private double moodConfidence;

    private TimeSeries heartRate;
    private TimeSeries hrv;
    private StepSeries steps;
    private SleepSeries sleep;

    private LlmMetaDto llm;

    @Data
    @Builder
    public static class DataPoint {
        private String ts;
        private Double value;
    }

    @Data
    public static class TimeSeries {
        private List<DataPoint> data;
        private int min;
        private int max;
        private double avg;
    }

    @Data
    public static class StepSeries {
        private List<DataPoint> data;
        private int total;
    }

    @Data
    public static class SleepSegment {
        private String start;
        private String end;
        private String stage;
        private int duration;
    }

    @Data
    public static class SleepSeries {
        private List<SleepSegment> segments;

        private int totalMinutes;
        private int lightMinutes;
        private int deepMinutes;
        private int remMinutes;
    }
}
