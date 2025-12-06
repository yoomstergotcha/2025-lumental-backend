package com.example.lumental.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "biometric_raw",
        indexes = {
                @Index(name = "idx_biometric_user_ts", columnList = "user_id, ts DESC")
        }
)
public class BiometricRaw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → users.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 측정 시점
    @Column(name = "ts", nullable = false)
    private OffsetDateTime ts;

    // heart_rate, hrv, sleep_stage 등
    @Column(name = "metric", nullable = false, length = 50)
    private String metric;

    // 수치형 값(심박수, HRV, 호흡수 등)
    @Column(name = "value_numeric")
    private Double valueNumeric;

    // JSONB(수면 단계 구간, 복합 데이터 등) -> 일단 String으로 저장


    @Column(columnDefinition = "text")
    private String valueJson;

    // apple_watch / iphone / manual 등
    @Column(name = "source", length = 30)
    private String source;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
