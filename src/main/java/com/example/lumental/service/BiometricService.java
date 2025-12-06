package com.example.lumental.service;

import com.example.lumental.domain.BiometricRaw;
import com.example.lumental.domain.BiometricSummaryDaily;
import com.example.lumental.domain.User;
import com.example.lumental.dto.*;
import com.example.lumental.repository.BiometricRawRepository;
import com.example.lumental.repository.BiometricSummaryDailyRepository;
import com.example.lumental.repository.UserRepository;
import com.example.lumental.util.HealthKitParser;
import com.example.lumental.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class BiometricService {

    private final BiometricRawRepository rawRepo;
    private final BiometricSummaryDailyRepository summaryRepo;
    private final UserRepository userRepo;
    private final WebClient webClient;
    private final HealthKitParser healthKitParser;

    // ==========================================================
    // A. 기존 DailyReport 플로우 (업로드 없이 API 요청받는 경우)
    // ==========================================================
    @Transactional
    public DailySummaryResponseDto processDailyReport(AnalysisRequestDto req) {

        // 1) RAW 저장
        saveRawMetrics(req);

        // 2) FastAPI 분석 호출
        AnalysisResultDto result = callAnalysisServer(req);

        // 3) summary 저장
        saveDailySummary(req.getUserId(), result);

        // 4) response DTO 생성
        return DailySummaryResponseDto.from(result);
    }

    // ---------------- RAW 저장 ----------------
    private void saveRawMetrics(AnalysisRequestDto req) {

        User user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        saveDataPoints(req.getHeartRate(), user, "heart_rate");
        saveDataPoints(req.getHrv(), user, "hrv");
        saveDataPoints(req.getSteps(), user, "steps");
        saveSleepSegments(req.getSleep(), user);
    }

    private void saveDataPoints(List<DataPointDto> list, User user, String metric) {
        if (list == null) return;

        for (DataPointDto dp : list) {
            rawRepo.save(
                    BiometricRaw.builder()
                            .user(user)
                            .metric(metric)
                            .ts(OffsetDateTime.parse(dp.getTs()))
                            .valueNumeric(dp.getValue())
                            .build()
            );
        }
    }

    private void saveSleepSegments(List<SleepSegmentDto> list, User user) {
        if (list == null) return;

        for (SleepSegmentDto s : list) {
            rawRepo.save(
                    BiometricRaw.builder()
                            .user(user)
                            .metric("sleep")
                            .ts(OffsetDateTime.parse(s.getStart()))
                            .valueJson(JsonUtils.toJson(s))
                            .build()
            );
        }
    }

    public File downloadZipFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            Path tempZip = Files.createTempFile("biometric", ".zip");
            try (InputStream in = url.openStream()) {
                Files.copy(in, tempZip, StandardCopyOption.REPLACE_EXISTING);
            }
            return tempZip.toFile();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download ZIP from GCS: " + e.getMessage());
        }
    }

    public AnalysisResultDto analyzeByFastApi(AnalysisRequestDto req) {
        return webClient.post()
                .uri("/analysis/daily")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(AnalysisResultDto.class)
                .block();
    }

     //---------------- FastAPI 호출 ----------------
    private AnalysisResultDto callAnalysisServer(AnalysisRequestDto req) {
        return webClient.post()
                .uri("/analysis/daily")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(AnalysisResultDto.class)
                .block();
    }

    // ---------------- Summary 저장 ----------------
    @Transactional
    public BiometricSummaryDaily saveDailySummary(Long userId, AnalysisResultDto result) {

        User user = userRepo.findById(userId)
                .orElseThrow();

        LocalDate date = LocalDate.parse(result.getSummaryDate());

        BiometricSummaryDaily summary = BiometricSummaryDaily.builder()
                .userId(user.getId())
                .summaryDate(date)

                .mood(result.getMood())
                .moodConfidence(result.getMoodConfidence())

                .heartRateMin(result.getHeartRate().getMin())
                .heartRateMax(result.getHeartRate().getMax())
                .heartRateAvg(result.getHeartRate().getAvg())

                .hrvAvg(result.getHrv().getAvg())

                .totalSteps(result.getSteps().getTotal())

                .sleepTotalMinutes(result.getSleep().getTotalMinutes())
                .sleepLightMinutes(result.getSleep().getLightMinutes())
                .sleepDeepMinutes(result.getSleep().getDeepMinutes())
                .sleepRemMinutes(result.getSleep().getRemMinutes())

                .calculatedStressLevel(result.getLlm().getCalculatedStressLevel())
                .dominantEmotion(result.getLlm().getDominantEmotion())
                .sentimentScore(result.getLlm().getSentimentScore())
                .summaryAdvice(result.getLlm().getSummaryAdvice())
                .detectedRisks(result.getLlm().getDetectedRisks())
                .build();

        return summaryRepo.save(summary);
    }

    // ==========================================================
    // B. 요약 조회 GET /api/biometric/report/{userId}?date=YYYY-MM-DD
    // ==========================================================
    @Transactional(readOnly = true)
    public DailySummaryResponseDto getDailySummary(Long userId, LocalDate date) {

        BiometricSummaryDaily summary = summaryRepo
                .findByUserIdAndSummaryDate(userId, date)
                .orElseThrow(() -> new RuntimeException("No summary found"));

        List<BiometricRaw> rawList = rawRepo.findAllByUserIdAndDate(userId, date);

        return DailySummaryResponseDto.from(summary, rawList);
    }

    @Transactional
    public DailySummaryResponseDto processUploadAndAnalyze(Long userId, File zipFile) {

        try {
            // ⬅ HealthKitParser에서 최종 DTO를 바로 만든다
            AnalysisRequestDto req = healthKitParser.parseZipToRequest(userId, zipFile);

            // FastAPI 분석
            AnalysisResultDto result = analyzeByFastApi(req);

            // DB 저장
            saveDailySummary(userId, result);

            // 프론트 응답 반환
            return DailySummaryResponseDto.from(result);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse HealthKit ZIP file", e);

        } finally {
            // 5) 임시 ZIP 삭제 (메모리/스토리지 누수 방지)
            if (zipFile != null && zipFile.exists()) {
                zipFile.delete();
            }
        }
    }


        public DailySummaryResponseDto getLatestSummary(Long userId){

            BiometricSummaryDaily summary = summaryRepo
                    .findTopByUserIdOrderBySummaryDateDesc(userId)
                    .orElseThrow(() -> new RuntimeException("Summary not found"));

            List<BiometricRaw> raw = rawRepo.findAllByUserIdAndDate(userId, summary.getSummaryDate());

            return DailySummaryResponseDto.from(summary, raw);
        }
    }
