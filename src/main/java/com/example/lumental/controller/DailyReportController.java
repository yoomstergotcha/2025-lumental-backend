package com.example.lumental.controller;

import com.example.lumental.domain.User;
import com.example.lumental.dto.DailyReportDto;
import com.example.lumental.service.BiometricService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/biometric-report")
@RequiredArgsConstructor
public class DailyReportController {

    private final BiometricService biometricService;

    /**
     * MVP 테스트 목적으로 userId를 임시 하드코딩한 버전
     * 추후 JWT 인증 붙이면 @AuthenticationPrincipal 로 대체됨
     */
    @GetMapping
    public DailyReportDto getDailyReport() {

        //  임시 유저 객체 (User 테이블이 완전히 연결되면 DB 조회로 변경)
        User dummyUser = User.builder()
                .id(1L)

                .build();

        return biometricService.getDailyReport(dummyUser);
    }
}
