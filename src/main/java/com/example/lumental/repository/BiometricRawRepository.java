package com.example.lumental.repository;

import com.example.lumental.domain.BiometricRaw;
import com.example.lumental.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface BiometricRawRepository extends JpaRepository<BiometricRaw, Long> {

    List<BiometricRaw> findTop100ByUserAndMetricOrderByTsDesc(User user, String metric);

    List<BiometricRaw> findByUserAndTsBetween(User user,
                                              OffsetDateTime start,
                                              OffsetDateTime end);
}

