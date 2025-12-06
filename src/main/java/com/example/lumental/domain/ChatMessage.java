package com.example.lumental.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;


@Entity
@Table(name = "chat_message")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String userMessage;
    @Column(columnDefinition = "TEXT")
    private String botReply;

    private String dominantEmotion;
    private Double sentimentScore;

    @Column(columnDefinition = "TEXT")
    private String recommendedCardsJson;

    private LocalDateTime createdAt;
}
