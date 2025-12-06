package com.example.lumental.dto;

import lombok.Data;
import java.util.List;
import com.example.lumental.dto.DataPointDto;
import com.example.lumental.dto.SleepSegmentDto;

@Data
public class GoogleFitRawResponse {

    private List<DataPointDto> heartRate;
    private List<DataPointDto> hrv;
    private List<DataPointDto> steps;
    private List<SleepSegmentDto> sleep;

}
