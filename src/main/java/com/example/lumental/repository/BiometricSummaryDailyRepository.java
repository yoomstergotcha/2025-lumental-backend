package com.example.lumental.repository;

import com.example.lumental.domain.BiometricSummaryDaily;
import com.example.lumental.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface BiometricSummaryDailyRepository extends JpaRepository<BiometricSummaryDaily, Long> {

    /**
     * userId + summaryDate 조합은 하루 하나의 요약 데이터가 존재해야 한다.
     * → SummaryDaily의 UniqueConstraint( user_id, summary_date )와 1:1 매칭
     */
    Optional<BiometricSummaryDaily> findByUserIdAndSummaryDate(Long userId, LocalDate summaryDate);
    Optional<BiometricSummaryDaily> findTopByUserIdOrderBySummaryDateDesc(Long userId);
    Optional<BiometricSummaryDaily> findFirstByUserIdOrderBySummaryDateDesc(Long userId);

}
