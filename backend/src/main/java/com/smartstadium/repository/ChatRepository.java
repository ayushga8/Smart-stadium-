package com.smartstadium.repository;

import com.smartstadium.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
}
