package com.example.lumental.controller;

import com.example.lumental.dto.AnalysisRequestDto;
import com.example.lumental.dto.DailySummaryResponseDto;
import com.example.lumental.dto.ApiResponse;
import com.example.lumental.dto.FileProcessRequestDto;
import com.example.lumental.service.BiometricService;


import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

import com.google.cloud.storage.HttpMethod;
import java.net.URL;

@RestController
@RequestMapping("/api/biometric")
@RequiredArgsConstructor
public class DailyReportController {

    private final BiometricService biometricService;
    private final Storage storage;

    /**
     * 1) 프론트에서 RAW 데이터 업로드
     * 2) 백엔드 → 분석서버 호출
     * 3) 분석 결과 DB 저장
     * 4) 최종 결과 프론트 반환
     */
    @PostMapping("/daily")
    public ApiResponse<DailySummaryResponseDto> analyze(@RequestBody AnalysisRequestDto request) {

        // 기본 검증
        if (request.getUserId() == null) {
            return ApiResponse.error("userId is required");
        }

        DailySummaryResponseDto result = biometricService.processDailyReport(request);
        return ApiResponse.success(result);
    }

    @PostMapping("/upload-url")
    public ResponseEntity<?> generateUploadUrl() {
        String objectName = "uploads/" + UUID.randomUUID() + ".zip";

        BlobInfo blobInfo = BlobInfo.newBuilder("lumental-bucket", objectName).build();

        URL signedUrl = storage.signUrl(
                blobInfo,
                10, TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withV4Signature()
        );

        Map<String, String> response = new HashMap<>();
        response.put("uploadUrl", signedUrl.toString());
        response.put("filePath", "gs://lumental-bucket/" + objectName);

        return ResponseEntity.ok(response);
    }

    /**
     * ZIP 업로드 → XML 추출 → 최신 날짜 데이터만 파싱 → FastAPI 분석 → DailySummaryResponseDto 반환
     */
    @PostMapping(value = "/upload/{userId}")
    public DailySummaryResponseDto uploadAndAnalyze(
            @PathVariable Long userId,
            @RequestBody FileProcessRequestDto requestDto
    ) throws Exception {

        String fileUrl = requestDto.getFileUrl();

        // 1) fileUrl에서 zip 파일 다운로드
        File downloadedZip = biometricService.downloadZipFromUrl(fileUrl);


        return biometricService.processUploadAndAnalyze(userId, downloadedZip);
    }

    @GetMapping("/report/{userId}")
    public DailySummaryResponseDto getDailyReport(
            @PathVariable Long userId,
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate summaryDate) {

        return biometricService.getDailySummary(userId, summaryDate);

    }

    @GetMapping("/report/{userId}/latest")
    public DailySummaryResponseDto getLatest(
            @PathVariable Long userId
    ) {
        return biometricService.getLatestSummary(userId);
    }

}
