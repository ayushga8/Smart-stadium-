package com.smartstadium.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChatMessage Entity")
class ChatMessageTest {

    @Test
    @DisplayName("builder creates entity with all fields")
    void builderWorks() {
        ChatMessage msg = ChatMessage.builder()
            .userId(1L)
            .userMessage("Hello")
            .aiResponse("Hi there!")
            .language("en")
            .build();

        assertEquals(1L, msg.getUserId());
        assertEquals("Hello", msg.getUserMessage());
        assertEquals("Hi there!", msg.getAiResponse());
        assertEquals("en", msg.getLanguage());
    }

    @Test
    @DisplayName("prePersist sets createdAt")
    void prePersistSetsTimestamp() {
        ChatMessage msg = ChatMessage.builder()
            .userId(1L).userMessage("Hi").aiResponse("Hello").build();
        msg.onCreate();
        assertNotNull(msg.getCreatedAt());
    }

    @Test
    @DisplayName("prePersist defaults language to en when null")
    void prePersistDefaultsLanguage() {
        ChatMessage msg = ChatMessage.builder()
            .userId(1L).userMessage("Hi").aiResponse("Hello").build();
        assertNull(msg.getLanguage());
        msg.onCreate();
        assertEquals("en", msg.getLanguage());
    }

    @Test
    @DisplayName("prePersist preserves existing language")
    void prePersistPreservesLanguage() {
        ChatMessage msg = ChatMessage.builder()
            .userId(1L).userMessage("Bonjour").aiResponse("Salut")
            .language("fr").build();
        msg.onCreate();
        assertEquals("fr", msg.getLanguage());
    }

    @Test
    @DisplayName("no-args constructor works")
    void noArgsConstructor() {
        ChatMessage msg = new ChatMessage();
        assertNull(msg.getId());
        assertNull(msg.getUserId());
    }

    @Test
    @DisplayName("all-args constructor works")
    void allArgsConstructor() {
        ChatMessage msg = new ChatMessage(1L, 2L, "Hi", "Hello", "en", null);
        assertEquals(1L, msg.getId());
        assertEquals(2L, msg.getUserId());
    }

    @Test
    @DisplayName("setters work")
    void settersWork() {
        ChatMessage msg = new ChatMessage();
        msg.setUserId(5L);
        msg.setUserMessage("test");
        msg.setAiResponse("reply");
        msg.setLanguage("es");

        assertEquals(5L, msg.getUserId());
        assertEquals("test", msg.getUserMessage());
        assertEquals("reply", msg.getAiResponse());
        assertEquals("es", msg.getLanguage());
    }

    @Test
    @DisplayName("equals and hashCode work")
    void equalsAndHashCode() {
        ChatMessage msg1 = ChatMessage.builder().userId(1L).userMessage("Hi").aiResponse("Hello").build();
        ChatMessage msg2 = ChatMessage.builder().userId(1L).userMessage("Hi").aiResponse("Hello").build();
        assertEquals(msg1, msg2);
        assertEquals(msg1.hashCode(), msg2.hashCode());
    }

    @Test
    @DisplayName("toString does not throw")
    void toStringWorks() {
        ChatMessage msg = ChatMessage.builder().userId(1L).userMessage("Hi").aiResponse("Hello").build();
        assertNotNull(msg.toString());
    }
}
