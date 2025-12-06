package com.example.lumental.domain;

import com.example.lumental.dto.AnalysisResultDto;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;


import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "biometric_summary_daily")
public class BiometricSummaryDaily {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    // ---------------------------
    // 🔹 생체 지표 요약
    // ---------------------------
    private Integer heartRateMin;
    private Integer heartRateMax;
    private Double heartRateAvg;

    private Integer hrvMin;
    private Integer hrvMax;
    private Double hrvAvg;

    private Integer totalSteps;

    private Integer sleepTotalMinutes;
    private Integer sleepLightMinutes;
    private Integer sleepDeepMinutes;
    private Integer sleepRemMinutes;

    // ---------------------------
    // 🔹 AI 분석 (LLM + classical signals)
    // ---------------------------
    private String mood;
    private Double moodConfidence;

    private Double calculatedStressLevel;
    private String dominantEmotion;
    private Double sentimentScore;

    @Column(columnDefinition = "TEXT")
    private String summaryAdvice;

    @ElementCollection
    @CollectionTable(
            name = "biometric_daily_risks",
            joinColumns = @JoinColumn(name = "summary_id")
    )
    @Column(name = "risk")
    private List<String> detectedRisks;

    // ---------------------------
    // 🔹 DTO → 엔티티 변환
    // ---------------------------
    public static BiometricSummaryDaily from(Long userId, AnalysisResultDto dto) {

        return BiometricSummaryDaily.builder()
                .userId(userId)
                .summaryDate(LocalDate.parse(dto.getSummaryDate()))

                // heartRate
                .heartRateMin(dto.getHeartRate().getMin())
                .heartRateMax(dto.getHeartRate().getMax())
                .heartRateAvg(dto.getHeartRate().getAvg())

                // HRV
                .hrvMin(dto.getHrv().getMin())
                .hrvMax(dto.getHrv().getMax())
                .hrvAvg(dto.getHrv().getAvg())

                // steps
                .totalSteps(dto.getSteps().getTotal())

                // sleep
                .sleepTotalMinutes(dto.getSleep().getTotalMinutes())
                .sleepLightMinutes(dto.getSleep().getLightMinutes())
                .sleepDeepMinutes(dto.getSleep().getDeepMinutes())
                .sleepRemMinutes(dto.getSleep().getRemMinutes())

                // mood
                .mood(dto.getMood())
                .moodConfidence(dto.getMoodConfidence())

                // LLM 기반 분석
                .calculatedStressLevel(dto.getLlm().getCalculatedStressLevel())
                .dominantEmotion(dto.getLlm().getDominantEmotion())
                .sentimentScore(dto.getLlm().getSentimentScore())
                .summaryAdvice(dto.getLlm().getSummaryAdvice())
                .detectedRisks(dto.getLlm().getDetectedRisks())

                .build();
    }
}

