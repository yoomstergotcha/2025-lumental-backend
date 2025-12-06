// Raw -> Spring -> FastAPI
package com.example.lumental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AnalysisRequestDto {

    private Long userId;
    private String timestamp;

    private List<DataPointDto> heartRate;
    private List<DataPointDto> hrv;
    private List<DataPointDto> steps;
    private List<SleepSegmentDto> sleep;
   // YYYY-MM-DD

    public static AnalysisRequestDto fromFit(Long userId, GoogleFitRawResponse fit) {
        return AnalysisRequestDto.builder()
                .userId(userId)
                .heartRate(fit.getHeartRate())
                .hrv(fit.getHrv())
                .steps(fit.getSteps())
                .sleep(fit.getSleep())

                .build();
    }

}
