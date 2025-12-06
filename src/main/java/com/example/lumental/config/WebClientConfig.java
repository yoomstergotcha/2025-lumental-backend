package com.example.lumental.config;

import com.example.lumental.dto.AnalysisRequestDto;
import com.example.lumental.dto.AnalysisResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import java.time.Duration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

@Configuration
public class WebClientConfig {

    @Value("${fastapi.analysis.base-url}")
    private String fastApiBaseUrl;

    @Bean
    public WebClient webClient() {
        // localhost 대신 127.0.0.1로 명시 (Docker 환경 보완)
        String finalBaseUrl = fastApiBaseUrl.replace("localhost", "127.0.0.1");

        ConnectionProvider provider = ConnectionProvider.builder("custom-provider")
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(10))
                .evictInBackground(Duration.ofSeconds(120))
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .responseTimeout(Duration.ofSeconds(120));

        // JSON 최대 16MB 허용
        final int size = 16 * 1024 * 1024;
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();

        return WebClient.builder()
                .baseUrl(finalBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }

    @Component
    @RequiredArgsConstructor
    public class AnalysisClient {

        private final WebClient webClient;

        public AnalysisResultDto analyzeDaily(AnalysisRequestDto dto) {
            return webClient.post()
                    .uri("/analysis/daily")
                    .bodyValue(dto)
                    .retrieve()
                    .bodyToMono(AnalysisResultDto.class)
                    .block();
        }
    }
}
