package com.example.lumental.runner;

import com.example.lumental.dto.AnalysisRequestDto;
import com.example.lumental.dto.AnalysisResultDto;
import com.example.lumental.dto.DataPointDto;
import com.example.lumental.dto.SleepSegmentDto;
import com.example.lumental.service.AnalysisClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import com.example.lumental.service.BiometricService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;


@RequiredArgsConstructor
@Profile("local") // ← local 환경에서만 실행됨
public class AnalysisRunner implements CommandLineRunner {

    private final AnalysisClientService analysisClientService;
    private final BiometricService biometricService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=================================================");
        System.out.println("🚀 FastAPI 통신 테스트 시작...");

        // 1. FastAPI 요청에 필요한 더미 데이터 생성
        AnalysisRequestDto request = createDummyRequestData();

        try {
            // 2. 서비스 호출 (실제 HTTP POST 통신 발생)
            AnalysisResultDto result = analysisClientService.fetchAnalysis(request);

            if (result != null) {
                System.out.println("✅ 통신 성공 및 DTO 매핑 완료!");
                System.out.println("   - 요약 날짜: " + result.getSummaryDate());
                System.out.println("   - 최종 감정: " + result.getMood());
                System.out.println("   - LLM 스트레스: " + result.getLlm().getCalculatedStressLevel());

                // ⭐ DB 저장 호출
                biometricService.saveDailySummary(request.getUserId(), result);
                System.out.println("💾 DB 저장 완료!");

            } else {
                System.out.println("❌ 통신 실패. 서비스 로그에서 오류 메시지를 확인하세요.");
            }
        } catch (Exception e) {
            // WebClientResponseException (4xx/5xx 응답) 또는 JsonMappingException 등이 여기서 잡힙니다.
            System.err.println("❌ 통신 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=================================================");
    }

    /**
     * FastAPI의 AnalysisRequest 스키마에 맞춰 더미 데이터를 생성하는 메서드 (실제 데이터로 대체해야 함)
     */
    private AnalysisRequestDto createDummyRequestData() {
        // 1. 심박수 (Heart Rate) 데이터 생성
        List<DataPointDto> hrData = List.of(
                DataPointDto.builder().ts("2025-11-12T20:00:00Z").value(88.0).build(),
                DataPointDto.builder().ts("2025-11-12T20:05:00Z").value(92.0).build(),
                DataPointDto.builder().ts("2025-11-12T20:10:00Z").value(110.0).build(),
                DataPointDto.builder().ts("2025-11-12T20:15:00Z").value(130.0).build(),
                DataPointDto.builder().ts("2025-11-12T20:20:00Z").value(150.0).build(),
                DataPointDto.builder().ts("2025-11-12T20:25:00Z").value(160.0).build(), // 높은 심박수 포함
                DataPointDto.builder().ts("2025-11-12T20:30:00Z").value(155.0).build(),
                DataPointDto.builder().ts("2025-11-12T20:35:00Z").value(140.0).build(),
                DataPointDto.builder().ts("2025-11-12T20:40:00Z").value(122.0).build(),
                DataPointDto.builder().ts("2025-11-12T20:45:00Z").value(105.0).build()
        );

        // 2. HRV 데이터 생성
        List<DataPointDto> hrvData = List.of(
                DataPointDto.builder().ts("2025-11-12T10:00:00Z").value(110.0).build(),
                DataPointDto.builder().ts("2025-11-12T13:00:00Z").value(180.0).build(),
                DataPointDto.builder().ts("2025-11-12T17:00:00Z").value(310.0).build()
                // ... 생략 (나머지 HRV 데이터도 동일하게 추가)
        );

        // 3. 걸음 수 (Steps) 데이터 생성
        List<DataPointDto> stepsData = List.of(
                DataPointDto.builder().ts("2025-11-12T08:00:00Z").value(120.0).build(),
                DataPointDto.builder().ts("2025-11-12T16:00:00Z").value(1500.0).build()
                // ... 생략 (나머지 걸음 수 데이터도 동일하게 추가)
        );

        // 4. 수면 (Sleep) 데이터 생성
        List<SleepSegmentDto> sleepData = List.of(
                SleepSegmentDto.builder().start("2025-11-11T23:00:00Z").end("2025-11-11T23:40:00Z").duration(40).stage("light").build(),
                SleepSegmentDto.builder().start("2025-11-11T23:40:00Z").end("2025-11-12T00:30:00Z").duration(50).stage("deep").build(),
                SleepSegmentDto.builder().start("2025-11-12T00:30:00Z").end("2025-11-12T01:00:00Z").duration(30).stage("rem").build(),
                SleepSegmentDto.builder().start("2025-11-12T01:00:00Z").end("2025-11-12T02:20:00Z").duration(80).stage("light").build(),
                SleepSegmentDto.builder().start("2025-11-12T02:20:00Z").end("2025-11-12T03:10:00Z").duration(50).stage("deep").build(),
                SleepSegmentDto.builder().start("2025-11-12T03:10:00Z").end("2025-11-12T05:00:00Z").duration(110).stage("light").build()
        );


        // 5. 최종 요청 DTO 빌드 및 반환
        return AnalysisRequestDto.builder()
                .userId(1L)
                .heartRate(hrData)
                .hrv(hrData) // HRV도 동일 데이터로 채워도 테스트에 문제 없습니다.
                .steps(stepsData)
                .sleep(sleepData)
                .build();
    }

}