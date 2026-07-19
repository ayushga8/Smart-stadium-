package com.smartstadium.service;

import com.smartstadium.entity.ChatMessage;
import com.smartstadium.repository.ChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiServiceTest {

    private ChatRepository chatRepository;
    private AiService aiService;

    @BeforeEach
    void setUp() {
        chatRepository = mock(ChatRepository.class);
        StadiumDataService stadiumDataService = new StadiumDataService();
        MatchService matchService = new MatchService();
        // No API key → uses built-in responses
        aiService = new AiService(chatRepository, stadiumDataService, matchService, "");
    }

    // === Built-in responses ===

    @ParameterizedTest
    @DisplayName("Gate-related queries return gate info")
    @ValueSource(strings = {"Where is gate A?", "How do I enter?", "Which entrance is closest?"})
    void gateQueriesReturnGateInfo(String message) {
        String response = aiService.chat(1L, message);
        assertTrue(response.contains("Gate"), "Response should mention gates");
    }

    @ParameterizedTest
    @DisplayName("Food queries return food info")
    @ValueSource(strings = {"Where can I eat?", "I'm hungry", "Any restaurants nearby?"})
    void foodQueriesReturnFoodInfo(String message) {
        String response = aiService.chat(1L, message);
        assertTrue(response.contains("Food"), "Response should mention food");
    }

    @ParameterizedTest
    @DisplayName("Restroom queries return restroom info")
    @ValueSource(strings = {"Where is the bathroom?", "I need a restroom", "Where are the toilets?"})
    void restroomQueriesReturnRestroomInfo(String message) {
        String response = aiService.chat(1L, message);
        assertTrue(response.contains("Restroom"), "Response should mention restrooms");
    }

    @ParameterizedTest
    @DisplayName("Accessibility queries return accessibility info")
    @ValueSource(strings = {"Where is the elevator?", "Wheelchair accessible?", "Disability support?"})
    void accessibilityQueriesReturnInfo(String message) {
        String response = aiService.chat(1L, message);
        assertTrue(response.contains("Accessib") || response.contains("Wheelchair"),
            "Response should mention accessibility");
    }

    @ParameterizedTest
    @DisplayName("Transport queries return transport info")
    @ValueSource(strings = {"How to get there by train?", "Where is parking?", "Bus to stadium?"})
    void transportQueriesReturnInfo(String message) {
        String response = aiService.chat(1L, message);
        assertTrue(response.contains("Park") || response.contains("Transit") || response.contains("Train"),
            "Response should mention transport");
    }

    @Test
    @DisplayName("Emergency queries return safety info")
    void emergencyQueriesReturnSafetyInfo() {
        String response = aiService.chat(1L, "I need medical help");
        assertTrue(response.contains("Emergency") || response.contains("First Aid"),
            "Response should mention emergency info");
    }

    @Test
    @DisplayName("Match queries return schedule info")
    void matchQueriesReturnSchedule() {
        String response = aiService.chat(1L, "When is the next game?");
        assertTrue(response.contains("Match") || response.contains("USA"),
            "Response should mention matches");
    }

    @Test
    @DisplayName("Greeting returns welcome message")
    void greetingReturnsWelcome() {
        String response = aiService.chat(1L, "Hello!");
        assertTrue(response.contains("Welcome") || response.contains("Hello"),
            "Response should greet the user");
    }

    @Test
    @DisplayName("Unknown queries return default help menu")
    void unknownQueriesReturnDefault() {
        String response = aiService.chat(1L, "What is the meaning of life?");
        assertTrue(response.contains("Stadium Assistant"),
            "Response should show the assistant menu");
    }

    @Test
    @DisplayName("WiFi queries return connectivity info")
    void wifiQueriesReturnInfo() {
        String response = aiService.chat(1L, "Is there wifi?");
        assertTrue(response.contains("WiFi") || response.contains("Charging"),
            "Response should mention connectivity");
    }

    @Test
    @DisplayName("Sustainability queries return eco info")
    void sustainabilityQueriesReturnInfo() {
        String response = aiService.chat(1L, "Where can I recycle?");
        assertTrue(response.contains("Recycl") || response.contains("Sustainability"),
            "Response should mention sustainability");
    }

    // === Chat persistence ===

    @Test
    @DisplayName("Chat message is persisted to repository")
    void chatMessagePersisted() {
        aiService.chat(1L, "Hello");
        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatRepository).save(captor.capture());
        ChatMessage saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals("Hello", saved.getUserMessage());
        assertNotNull(saved.getAiResponse());
    }

    @Test
    @DisplayName("getHistory delegates to repository")
    void getHistoryDelegatesToRepo() {
        when(chatRepository.findTop20ByUserIdOrderByCreatedAtDesc(1L))
            .thenReturn(List.of());
        List<ChatMessage> history = aiService.getHistory(1L);
        assertTrue(history.isEmpty());
        verify(chatRepository).findTop20ByUserIdOrderByCreatedAtDesc(1L);
    }

    // === Language detection ===

    @ParameterizedTest
    @DisplayName("Language detection identifies languages correctly")
    @CsvSource({
        "'Hello world', en",
        "'Bonjour comment ça va', fr",
        "'Hola gracias', es",
        "'Olá obrigado', pt"
    })
    void languageDetection(String text, String expectedLang) {
        aiService.chat(1L, text);
        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatRepository).save(captor.capture());
        assertEquals(expectedLang, captor.getValue().getLanguage());
    }
}
