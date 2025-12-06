package com.example.lumental.service;

import com.example.lumental.dto.AnalysisRequestDto;
import com.example.lumental.dto.AnalysisResultDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AnalysisClientService {

    private final WebClient webClient;

    // WebClientConfig에서 정의한 Bean이 자동으로 주입됩니다.
    public AnalysisClientService(WebClient fastapiWebClient) {
        this.webClient = fastapiWebClient;
    }

    public AnalysisResultDto fetchAnalysis(AnalysisRequestDto requestDto) {
        try {
            return webClient.post()
                    .uri("/daily") // Base URL(http://localhost:8001/analysis)에 "/daily"가 붙어 통신
                    .bodyValue(requestDto) // FastAPI의 AnalysisRequest와 일치하는 DTO 전송
                    .retrieve()
                    // FastAPI에서 HTTP 4xx/5xx 응답이 오면 여기서 WebClientResponseException 발생
                    .bodyToMono(AnalysisResultDto.class) // FastAPI 응답을 DTO로 역직렬화
                    .block();
        } catch (Exception e) {
            // 통신 실패(ConnectException) 또는 DTO 매핑 실패(JsonMappingException) 시 로그 출력
            System.err.println("FastAPI 통신 실패 또는 응답 처리 오류: " + e.getMessage());
            e.printStackTrace();
            return null; // 실패 시 null 반환 또는 사용자 정의 에러 처리
        }
    }
}