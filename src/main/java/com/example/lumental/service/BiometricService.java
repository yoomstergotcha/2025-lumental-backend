package com.example.lumental.service;

import com.example.lumental.domain.BiometricSummaryDaily;
import com.example.lumental.domain.User;
import com.example.lumental.dto.DailyReportDto;
import com.example.lumental.repository.BiometricSummaryDailyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BiometricService {

    private final BiometricSummaryDailyRepository summaryDailyRepository;

    /**
     * 하루 요약 리포트 가져오기
     * 1) DB에 summary가 있으면 바로 반환
     * 2) 없으면 dummy or 분석서버 호출해 summary 생성 (MVP 단계라 dummy)
     */
    public DailyReportDto getDailyReport(User user) {

        LocalDate today = LocalDate.now();

        // 1) DB SummaryDaily 조회
        BiometricSummaryDaily summary = summaryDailyRepository
                .findByUserAndSummaryDate(user, today)
                .orElse(null);

        if (summary == null) {
            // 아직 SummaryDaily가 없다면 → 우선 dummy 생성 (MVP)
            summary = createDummySummary(user, today);
            summaryDailyRepository.save(summary);
        }

        // 2) Summary 엔티티 → DTO 변환 후 반환
        return convertToDto(summary);
    }

    /**
     * Dummy SummaryDaily 생성 (MVP 단계)
     */
    private BiometricSummaryDaily createDummySummary(User user, LocalDate date) {

        return BiometricSummaryDaily.builder()
                .user(user)
                .summaryDate(date)
                .mood("우울")
                .moodConfidence(0.78)

                .heartRateMin(53)
                .heartRateMax(183)
                .heartRateJson("""
                        {
                          "data": [
                            {"ts": "2025-01-12T21:05:00", "value": 171},
                            {"ts": "2025-01-12T21:03:00", "value": 160},
                            {"ts": "2025-01-12T21:01:00", "value": 142},
                            {"ts": "2025-01-12T20:59:00", "value": 130},
                            {"ts": "2025-01-12T20:57:00", "value": 124}
                          ]
                        }
                        """)

                .hrvAvg(69.0)
                .hrvJson("""
                        {
                          "data": [
                            {"ts": "2025-01-12T20:30:00", "value": 310},
                            {"ts": "2025-01-12T20:20:00", "value": 200},
                            {"ts": "2025-01-12T20:10:00", "value": 150}
                          ]
                        }
                        """)

                .stepsTotal(7234)
                .stepsJson("""
                        {
                          "data": [
                            {"ts": "2025-01-12T21:00:00", "value": 300},
                            {"ts": "2025-01-12T20:00:00", "value": 920},
                            {"ts": "2025-01-12T19:00:00", "value": 700}
                          ]
                        }
                        """)

                .sleepTotalMinutes(451)
                .sleepDeepMinutes(102)
                .sleepLightMinutes(230)
                .sleepRemMinutes(119)
                .sleepSegmentsJson("""
                        [
                          {"start": "2025-01-11T23:12:00", "end": "2025-01-12T00:30:00", "stage": "light"},
                          {"start": "2025-01-12T00:30:00", "end": "2025-01-12T01:18:00", "stage": "deep"},
                          {"start": "2025-01-12T01:18:00", "end": "2025-01-12T02:23:00", "stage": "rem"}
                        ]
                        """)

                .build();
    }

    /**
     * SummaryDaily → DailyReportDto 변환
     */
    private DailyReportDto convertToDto(BiometricSummaryDaily summary) {

        return DailyReportDto.builder()
                .summaryDate(summary.getSummaryDate().toString())
                .mood(summary.getMood())
                .moodConfidence(summary.getMoodConfidence())

                .heartRate(Map.of(
                        "min", summary.getHeartRateMin(),
                        "max", summary.getHeartRateMax(),
                        "dataJson", summary.getHeartRateJson()
                ))
                .hrv(Map.of(
                        "avg", summary.getHrvAvg(),
                        "dataJson", summary.getHrvJson()
                ))
                .stepCount(Map.of(
                        "total", summary.getStepsTotal(),
                        "dataJson", summary.getStepsJson()
                ))
                .sleep(Map.of(
                        "totalMinutes", summary.getSleepTotalMinutes(),
                        "deepMinutes", summary.getSleepDeepMinutes(),
                        "lightMinutes", summary.getSleepLightMinutes(),
                        "remMinutes", summary.getSleepRemMinutes(),
                        "segmentsJson", summary.getSleepSegmentsJson()
                ))

                .build();
    }
}
