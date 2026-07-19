package com.smartstadium.controller;

import com.smartstadium.dto.ChatRequestDto;
import com.smartstadium.dto.ChatResponseDto;
import com.smartstadium.entity.ChatMessage;
import com.smartstadium.entity.User;
import com.smartstadium.exception.UserNotFoundException;
import com.smartstadium.repository.UserRepository;
import com.smartstadium.service.AiService;
import com.smartstadium.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDto> chat(
            @RequestBody ChatRequestDto request,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserId(authHeader);
        String response = aiService.chat(userId, request.getMessage());

        return ResponseEntity.ok(ChatResponseDto.builder()
                .response(response)
                .language("en")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> history(
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserId(authHeader);
        List<ChatMessage> messages = aiService.getHistory(userId);

        List<Map<String, Object>> result = messages.stream().map(m -> Map.<String, Object>of(
                "userMessage", m.getUserMessage(),
                "aiResponse", m.getAiResponse(),
                "language", m.getLanguage(),
                "timestamp", m.getCreatedAt().toString()
        )).toList();

        return ResponseEntity.ok(result);
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        return user.getId();
    }
}
