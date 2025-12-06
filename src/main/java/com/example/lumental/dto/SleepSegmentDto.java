package com.example.lumental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SleepSegmentDto {
    private String start;
    private String end;
    private Integer duration;
    private String stage;
}
