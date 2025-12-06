package com.example.lumental.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class ChatResponseDto {

    private String reply;
    private String dominantEmotion;
    private double sentimentScore;

    private List<RecommendedCard> recommendedCards;

    @Getter @Setter
    public static class RecommendedCard {
        private String title;
        private String description;
        private String type;
    }
}
