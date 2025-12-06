package com.example.lumental.repository;

import com.example.lumental.domain.BiometricRaw;
import com.example.lumental.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.time.LocalDate;


public interface BiometricRawRepository extends JpaRepository<BiometricRaw, Long> {

    List<BiometricRaw> findTop100ByUserAndMetricOrderByTsDesc(User user, String metric);

    List<BiometricRaw> findByUserAndTsBetween(User user,
                                              OffsetDateTime start,
                                              OffsetDateTime end);
    @Query("SELECT r FROM BiometricRaw r WHERE r.user.id = :userId AND CAST(r.ts AS date) = :date")
    List<BiometricRaw> findAllByUserIdAndDate(@Param("userId") Long userId,
                                              @Param("date") LocalDate date);
}

