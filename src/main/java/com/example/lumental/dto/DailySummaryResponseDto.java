//Spring → 프론트
package com.example.lumental.dto;

import com.example.lumental.domain.BiometricRaw;
import com.example.lumental.domain.BiometricSummaryDaily;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySummaryResponseDto {

    private String summaryDate;
    private String mood;
    private double moodConfidence;

    private TimeSeries heartRate;
    private TimeSeries hrv;
    private StepSeries steps;
    private SleepSeries sleep;

    private LlmResult llm;

    // ---------- nested DTOs ----------

    @Data
    @Builder
    public static class TimeSeries {
        private Double min;
        private Double max;
        private Double avg;
        private List<DataPoint> data;
    }

    @Data
    @Builder
    public static class StepSeries {
        private Integer total;
        private List<DataPoint> data;
    }

    @Data
    @Builder
    public static class DataPoint {
        private String ts;
        private Double value;
    }

    @Data
    @Builder
    public static class SleepSeries {
        private Integer totalMinutes;
        private Integer lightMinutes;
        private Integer deepMinutes;
        private Integer remMinutes;
        private List<SleepSegment> segments;
    }

    @Data
    @Builder
    public static class SleepSegment {
        private String start;
        private String end;
        private String stage;
        private Integer duration;
    }

    @Data
    @Builder
    public static class LlmResult {
        private Double calculatedStressLevel;
        private String dominantEmotion;
        private Double sentimentScore;
        private String summaryAdvice;
        private List<String> detectedRisks;
    }


    // ---------- FastAPI -> Response 직접 변환 ----------

    public static DailySummaryResponseDto from(AnalysisResultDto dto) {

        return DailySummaryResponseDto.builder()
                .summaryDate(dto.getSummaryDate())
                .mood(dto.getMood())
                .moodConfidence(dto.getMoodConfidence())

                .heartRate(TimeSeries.builder()
                        // 🔥 dto 쪽은 primitive int 이라서 그냥 double 캐스팅
                        .min((double) dto.getHeartRate().getMin())
                        .max((double) dto.getHeartRate().getMax())
                        .avg(dto.getHeartRate().getAvg())
                        .data(dto.getHeartRate().getData().stream()
                                .map(dp -> DataPoint.builder()
                                        .ts(dp.getTs())
                                        .value(dp.getValue())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())

                .hrv(TimeSeries.builder()
                        // 필요하면 FastAPI에서 min/max 추가 가능
                        .min(null)
                        .max(null)
                        .avg(dto.getHrv().getAvg())
                        .data(dto.getHrv().getData().stream()
                                .map(dp -> DataPoint.builder()
                                        .ts(dp.getTs())
                                        .value(dp.getValue())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())

                .steps(StepSeries.builder()
                        .total(dto.getSteps().getTotal())
                        .data(dto.getSteps().getData().stream()
                                .map(dp -> DataPoint.builder()
                                        .ts(dp.getTs())
                                        .value(dp.getValue())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())

                .sleep(SleepSeries.builder()
                        .totalMinutes(dto.getSleep().getTotalMinutes())
                        .lightMinutes(dto.getSleep().getLightMinutes())
                        .deepMinutes(dto.getSleep().getDeepMinutes())
                        .remMinutes(dto.getSleep().getRemMinutes())
                        .segments(dto.getSleep().getSegments().stream()
                                .map(seg -> SleepSegment.builder()
                                        .start(seg.getStart())
                                        .end(seg.getEnd())
                                        .stage(seg.getStage())
                                        .duration(seg.getDuration())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())

                .llm(LlmResult.builder()
                        .calculatedStressLevel(dto.getLlm().getCalculatedStressLevel())
                        .dominantEmotion(dto.getLlm().getDominantEmotion())
                        .sentimentScore(dto.getLlm().getSentimentScore())
                        .summaryAdvice(dto.getLlm().getSummaryAdvice())
                        .detectedRisks(dto.getLlm().getDetectedRisks())
                        .build())

                .build();
    }

    // ---------- DB 요약 + RAW -> Response ----------

    private static DataPoint rawToDataPoint(BiometricRaw r) {
        return DataPoint.builder()
                .ts(r.getTs().toString())
                .value(r.getValueNumeric())
                .build();
    }

    public static DailySummaryResponseDto from(BiometricSummaryDaily s, List<BiometricRaw> rawList) {

        List<DataPoint> heartRateData = rawList.stream()
                .filter(r -> "heart_rate".equals(r.getMetric()))
                .map(DailySummaryResponseDto::rawToDataPoint)
                .collect(Collectors.toList());

        List<DataPoint> hrvData = rawList.stream()
                .filter(r -> "hrv".equals(r.getMetric()))
                .map(DailySummaryResponseDto::rawToDataPoint)
                .collect(Collectors.toList());

        List<DataPoint> stepData = rawList.stream()
                .filter(r -> "steps".equals(r.getMetric()))
                .map(DailySummaryResponseDto::rawToDataPoint)
                .collect(Collectors.toList());

        return DailySummaryResponseDto.builder()
                .summaryDate(s.getSummaryDate().toString())
                .mood(s.getMood())
                .moodConfidence(s.getMoodConfidence())

                .heartRate(TimeSeries.builder()
                        // 🔥 BiometricSummaryDaily 쪽 heartRateMin/Max도 primitive int 라고 가정
                        .min(Double.valueOf(s.getHeartRateMin()))
                        .max(Double.valueOf(s.getHeartRateMax()))
                        .avg(s.getHeartRateAvg())
                        .data(heartRateData)
                        .build())

                .hrv(TimeSeries.builder()
                        .min(null)
                        .max(null)
                        .avg(s.getHrvAvg())
                        .data(hrvData)
                        .build())

                .steps(StepSeries.builder()
                        .total(s.getTotalSteps())
                        .data(stepData)
                        .build())

                .sleep(SleepSeries.builder()
                        .totalMinutes(s.getSleepTotalMinutes())
                        .lightMinutes(s.getSleepLightMinutes())
                        .deepMinutes(s.getSleepDeepMinutes())
                        .remMinutes(s.getSleepRemMinutes())
                        // MVP에선 sleep segments를 DB에서 다시 복원하지 않고 빈 리스트로 둠
                        .segments(List.of())
                        .build())

                .llm(LlmResult.builder()
                        .calculatedStressLevel(s.getCalculatedStressLevel())
                        .dominantEmotion(s.getDominantEmotion())
                        .sentimentScore(s.getSentimentScore())
                        .summaryAdvice(s.getSummaryAdvice())
                        .detectedRisks(s.getDetectedRisks())
                        .build())

                .build();
    }
}
