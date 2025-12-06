package com.example.lumental.service;

import com.example.lumental.domain.BiometricSummaryDaily;
import com.example.lumental.domain.ChatMessage;
import com.example.lumental.dto.ChatMessageRequest;
import com.example.lumental.dto.ChatAnalysisRequest;
import com.example.lumental.dto.ChatResponseDto;
import com.example.lumental.repository.BiometricSummaryDailyRepository;
import com.example.lumental.repository.ChatMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final WebClient webClient;
    private final BiometricSummaryDailyRepository summaryRepo;
    private final ChatMessageRepository chatMessageRepository;

    private final ChatMessageRepository chatMessageRepo;   // ← DB 저장용
    private final ObjectMapper objectMapper;

    @Transactional
    public ChatResponseDto handleChat(ChatMessageRequest req) {

        // 1) 최근 summary 가져오기
        BiometricSummaryDaily latest = summaryRepo
                .findFirstByUserIdOrderBySummaryDateDesc(req.getUserId())
                .orElse(null);

        // 2) bio_context 구성
        Map<String, Object> ctx = new HashMap<>();

        if (latest != null) {
            ctx.put("heart_rate", latest.getHeartRateAvg());
            ctx.put("sleep_quality", calcSleepQuality(latest));
            ctx.put("stress_level", latest.getCalculatedStressLevel());
            ctx.put("steps", latest.getTotalSteps());
            ctx.put("calories", 210); // HealthKit 아직 미지원 → placeholder
        }

        // 3) FastAPI 요청 DTO 구성
        ChatAnalysisRequest fastapiReq = ChatAnalysisRequest.builder()
                .userId(req.getUserId())
                .userMessage(req.getMessage())
                .bio_context(ctx)
                .build();

        // 4) FastAPI 호출
        ChatResponseDto result = webClient.post()
                .uri("/analysis/chat")
                .bodyValue(fastapiReq)
                .retrieve()
                .bodyToMono(ChatResponseDto.class)
                .block();

        String cardsJson = "[]";
        try {
            cardsJson = objectMapper.writeValueAsString(result.getRecommendedCards());
        } catch (Exception ignore) {}

        // 5) DB 저장 로직 추가
        ChatMessage entity = ChatMessage.builder()
                .userId(req.getUserId())
                .userMessage(req.getMessage())
                .botReply(result.getReply())
                .dominantEmotion(result.getDominantEmotion())
                .sentimentScore(result.getSentimentScore())
                .recommendedCardsJson(cardsJson)   // JSON 통째로 저장
                .createdAt(LocalDateTime.now())
                .build();

        chatMessageRepo.save(entity);

        return result;
    }

    private void saveChatLog(ChatMessageRequest req, ChatResponseDto ai) {

        ChatMessage entity = ChatMessage.builder()
                .userId(req.getUserId())
                .userMessage(req.getMessage())
                .botReply(ai.getReply())
                .dominantEmotion(ai.getDominantEmotion())
                .sentimentScore(ai.getSentimentScore())
                .recommendedCardsJson(toJson(ai.getRecommendedCards()))
                .createdAt(LocalDateTime.now())
                .build();

        chatMessageRepository.save(entity);
    }

    private String toJson(Object o) {
        try {
            return new ObjectMapper().writeValueAsString(o);
        } catch (Exception e) {
            return "[]";
        }
    }

    private double calcSleepQuality(BiometricSummaryDaily s) {
        int total = s.getSleepTotalMinutes();
        if (total == 0) return 0.0;

        // 간단한 가중치 기반 sleep quality 모델
        double score =
                (s.getSleepDeepMinutes() * 1.2 +
                        s.getSleepRemMinutes() * 1.0 +
                        s.getSleepLightMinutes() * 0.6) / total;

        return Math.min(1.0, score);
    }
}
