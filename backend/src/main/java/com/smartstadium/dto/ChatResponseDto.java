package com.smartstadium.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ChatResponseDto {
    private String response;
    private String language;
    private LocalDateTime timestamp;
}
