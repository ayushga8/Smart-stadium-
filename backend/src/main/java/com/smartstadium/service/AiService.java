package com.smartstadium.service;

import com.smartstadium.entity.ChatMessage;
import com.smartstadium.repository.ChatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AiService {

    private final WebClient webClient;
    private final ChatRepository chatRepository;
    private final StadiumDataService stadiumDataService;
    private final MatchService matchService;
    private final String apiKey;

    public AiService(
            ChatRepository chatRepository,
            StadiumDataService stadiumDataService,
            MatchService matchService,
            @Value("${app.gemini.api-key:}") String apiKey) {
        this.chatRepository = chatRepository;
        this.stadiumDataService = stadiumDataService;
        this.matchService = matchService;
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public String chat(Long userId, String userMessage) {
        String response;

        if (apiKey == null || apiKey.isBlank()) {
            log.info("No Gemini API key configured — using built-in responses");
            response = getBuiltInResponse(userMessage);
        } else {
            response = callGemini(userMessage);
        }

        // Persist chat
        chatRepository.save(ChatMessage.builder()
                .userId(userId)
                .userMessage(userMessage)
                .aiResponse(response)
                .language(detectLanguage(userMessage))
                .build());

        return response;
    }

    public List<ChatMessage> getHistory(Long userId) {
        return chatRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId);
    }

    private String callGemini(String userMessage) {
        try {
            String systemPrompt = buildSystemPrompt();

            Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents", List.of(
                    Map.of("role", "user", "parts", List.of(Map.of("text", userMessage)))
                ),
                "generationConfig", Map.of(
                    "temperature", 0.7,
                    "maxOutputTokens", 1024
                )
            );

            Map<?, ?> result = webClient.post()
                .uri("/v1beta/models/gemini-2.0-flash:generateContent?key={key}", apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (result != null && result.containsKey("candidates")) {
                List<?> candidates = (List<?>) result.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
                    Map<?, ?> content = (Map<?, ?>) candidate.get("content");
                    List<?> parts = (List<?>) content.get("parts");
                    Map<?, ?> part = (Map<?, ?>) parts.get(0);
                    return (String) part.get("text");
                }
            }
            return "I'm having trouble processing that right now. Please try again!";
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            return getBuiltInResponse(userMessage);
        }
    }

    private String buildSystemPrompt() {
        return """
            You are the FIFA World Cup 2026 Smart Stadium AI Assistant. You help fans, staff, and volunteers \
            at MetLife Stadium in East Rutherford, New Jersey.

            YOUR CAPABILITIES:
            - Stadium navigation and wayfinding
            - Real-time crowd density information
            - Match schedules and team information
            - Food, drinks, and merchandise locations
            - Accessibility information (wheelchair routes, elevators, assistance)
            - Transportation guidance (parking, public transit, rideshare)
            - Sustainability tips (recycling, water stations)
            - Multilingual support — respond in the user's language
            - Emergency procedures and first aid locations

            RULES:
            - Be friendly, concise, and helpful
            - Use emojis sparingly for warmth
            - If unsure, direct users to the nearest information desk
            - Always prioritize safety information
            - For emergencies, tell users to call stadium security at ext. 911

            CURRENT STADIUM DATA:
            """ + stadiumDataService.getContextSummary() + "\n\n" + matchService.getContextSummary();
    }

    private String getBuiltInResponse(String message) {
        String lower = message.toLowerCase();

        if (lower.contains("gate") || lower.contains("entrance") || lower.contains("enter")) {
            return "🚪 **Stadium Gates:**\n- **Gate A** (North) — Main entrance, wheelchair accessible\n- **Gate B** (South) — Closest to parking lots\n- **Gate C** (East) — Near transit hub\n- **Gate D** (West) — VIP & media entrance\n\nGates open 2 hours before kickoff. Have your digital ticket ready!";
        }
        if (lower.contains("food") || lower.contains("eat") || lower.contains("hungry") || lower.contains("restaurant")) {
            return "🍔 **Food Options:**\n- **Food Court Alpha** (Ground Level, North) — Stadium Grill, Global Bites\n- **Food Court Beta** (Upper Level) — Pizza Corner, Taco Stand\n\n💡 **Tip:** Food Court Alpha is currently less crowded. Skip the lines!";
        }
        if (lower.contains("restroom") || lower.contains("bathroom") || lower.contains("toilet")) {
            return "🚻 **Restrooms:**\n- **North Lower** — Near Section 102 (♿ accessible)\n- **South Lower** — Near Section 202 (♿ accessible)\n- **Upper East** — Near Section 310\n\n♿ All lower level restrooms are wheelchair accessible.";
        }
        if (lower.contains("wheelchair") || lower.contains("accessible") || lower.contains("disability") || lower.contains("elevator")) {
            return "♿ **Accessibility:**\n- Wheelchair ramps at Gate A (ground level)\n- Elevator at Lower East stand (all levels)\n- Accessible restrooms at North & South Lower\n- Assistance available — ask any volunteer in yellow!\n\nNeed help? Call stadium assistance at ext. 555.";
        }
        if (lower.contains("parking") || lower.contains("car") || lower.contains("drive")) {
            return "🅿️ **Parking:**\n- Lots open 4 hours before kickoff\n- **Lot A** (closest) — $60\n- **Lot B/C** — $45\n- Pre-book at parking.metlifestadium.com for guaranteed spots\n\n🚇 Consider NJ Transit — direct trains from Penn Station!";
        }
        if (lower.contains("train") || lower.contains("transit") || lower.contains("transport") || lower.contains("bus")) {
            return "🚇 **Public Transit:**\n- **NJ Transit** — Meadowlands Rail from Secaucus Junction (~15 min)\n- **NY Penn Station** → Secaucus → Meadowlands (~40 min)\n- **Bus 160** from Port Authority\n- **Rideshare** drop-off at Lot J\n\n💡 Transit is recommended — no parking stress!";
        }
        if (lower.contains("recycle") || lower.contains("sustainability") || lower.contains("green") || lower.contains("environment")) {
            return "♻️ **Sustainability:**\n- **Recycling points** at North & South Concourses\n- **Hydration stations** for free water refills (bring your bottle!)\n- MetLife Stadium runs on 30% solar power during events\n- Please sort: 🥤 Plastics | 📄 Paper | 🥫 Cans\n\n🌍 Help us make this the greenest World Cup ever!";
        }
        if (lower.contains("match") || lower.contains("schedule") || lower.contains("game") || lower.contains("play")) {
            return "⚽ **Upcoming Matches at MetLife Stadium:**\n- 🇺🇸 USA vs Morocco 🇲🇦 — Jun 11, 5:00 PM\n- 🇩🇪 Germany vs Japan 🇯🇵 — Jun 12, 8:00 PM\n- 🇫🇷 France vs South Korea 🇰🇷 — Jun 13, 8:00 PM\n- 🇳🇱 Netherlands vs Ecuador 🇪🇨 — Jun 15, 2:00 PM\n\nUse the Match Schedule tab for the full lineup!";
        }
        if (lower.contains("emergency") || lower.contains("help") || lower.contains("medical") || lower.contains("first aid")) {
            return "🚨 **Emergency Info:**\n- **First Aid** stations at North & South Concourses\n- **Security** — Call ext. 911 from any stadium phone\n- **Emergency exits** marked with green signs at every section\n- Nearest hospital: Hackensack University Medical Center (10 min)\n\n⚠️ For life-threatening emergencies, call 911 immediately.";
        }
        if (lower.contains("wifi") || lower.contains("internet") || lower.contains("charg")) {
            return "📶 **Connectivity:**\n- **Free WiFi**: Connect to `FIFA2026_FanNet`\n- **Phone Charging Hub** at North Concourse (free USB/wireless)\n- Pro tip: Download the stadium map offline before the match!";
        }
        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return "👋 **Hello!** Welcome to MetLife Stadium for FIFA World Cup 2026! 🏟️⚽\n\nI'm your AI Stadium Assistant. I can help with:\n- 🗺️ Navigation & directions\n- 🍔 Food & drinks\n- 🚇 Transportation\n- ♿ Accessibility\n- ⚽ Match schedules\n- ♻️ Sustainability\n\nWhat can I help you with?";
        }

        // Default response
        return "🏟️ I'm your Smart Stadium Assistant! I can help with:\n\n" +
               "- **Navigation** — \"How do I get to Gate B?\"\n" +
               "- **Food** — \"Where can I eat?\"\n" +
               "- **Matches** — \"When is the next game?\"\n" +
               "- **Transport** — \"How do I get here by train?\"\n" +
               "- **Accessibility** — \"Where are the elevators?\"\n" +
               "- **Sustainability** — \"Where can I recycle?\"\n\n" +
               "Just ask me anything about the stadium! 😊";
    }

    private String detectLanguage(String text) {
        // Simple heuristic — could be replaced by Gemini's detection
        if (text.matches(".*[\\u3040-\\u309F\\u30A0-\\u30FF].*")) return "ja";
        if (text.matches(".*[\\u4E00-\\u9FFF].*")) return "zh";
        if (text.matches(".*[\\u0600-\\u06FF].*")) return "ar";
        if (text.matches(".*[\\u0900-\\u097F].*")) return "hi";
        if (text.matches(".*[à-ÿÀ-ß].*") && text.toLowerCase().matches(".*(bonjour|merci|comment|où).*")) return "fr";
        if (text.toLowerCase().matches(".*(hola|gracias|dónde|cómo).*")) return "es";
        if (text.toLowerCase().matches(".*(olá|obrigado|onde|como).*")) return "pt";
        return "en";
    }
}
