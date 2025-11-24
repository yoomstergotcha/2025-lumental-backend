
package com.example.lumental.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "biometric_summary_daily",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_biometric_summary_user_date", columnNames = {"user_id", "summary_date"})
        }
)
public class BiometricSummaryDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 (FK → users.id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 그날의 날짜 (YYYY-MM-DD)
    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    // -------------- 감정 분석 요약 ---------------- //
    @Column(name = "mood", length = 20)
    private String mood;  // 우울, 스트레스, 안정 등

    @Column(name = "mood_confidence")
    private Double moodConfidence;  // 0~1

    // -------------- 심박수 요약 ---------------- //
    @Column(name = "heart_rate_min")
    private Integer heartRateMin;

    @Column(name = "heart_rate_max")
    private Integer heartRateMax;

    @Column(name = "heart_rate_json", columnDefinition = "jsonb")
    private String heartRateJson; // {data:[{ts,value}]}

    // -------------- HRV 요약 ---------------- //
    @Column(name = "hrv_avg")
    private Double hrvAvg;

    @Column(name = "hrv_json", columnDefinition = "jsonb")
    private String hrvJson;

    // -------------- 걸음수 요약 ---------------- //
    @Column(name = "steps_total")
    private Integer stepsTotal;

    @Column(name = "steps_json", columnDefinition = "jsonb")
    private String stepsJson;

    // -------------- 수면 요약 ---------------- //
    @Column(name = "sleep_total_minutes")
    private Integer sleepTotalMinutes;

    @Column(name = "sleep_deep_minutes")
    private Integer sleepDeepMinutes;

    @Column(name = "sleep_light_minutes")
    private Integer sleepLightMinutes;

    @Column(name = "sleep_rem_minutes")
    private Integer sleepRemMinutes;

    @Column(name = "sleep_segments_json", columnDefinition = "jsonb")
    private String sleepSegmentsJson; // [{start,end,stage}]

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}


