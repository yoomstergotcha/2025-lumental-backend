package com.example.lumental.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiometricBulkUploadRequestDto {

    private Long userId;   // 또는 externalId를 쓸 수도 있음

    private List<BiometricSampleDto> samples;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BiometricSampleDto {
        private String metric;           // "heart_rate", "hrv", ...
        private Double valueNumeric;
        private String valueJson;        // optional JSON string
        private String source;           // "apple_watch"
        private OffsetDateTime ts;       // 측정 시각
    }
}

