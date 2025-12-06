package com.example.lumental.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="users")
public class User {

    @Id @GeneratedValue
    private Long id;

    private String nickname;

    private LocalDateTime createdAt;
}
