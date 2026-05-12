package com.basitborsa.service;

import com.basitborsa.dto.ai.*;
import com.basitborsa.entity.AiExplanation;
import com.basitborsa.repository.AiExplanationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.basitborsa.util.AppConstants.AI_DISCLAIMER;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final WebClient geminiWebClient;
    private final AiExplanationRepository aiExplanationRepository;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    public AiService(WebClient geminiWebClient,
                     AiExplanationRepository aiExplanationRepository,
                     ObjectMapper objectMapper) {
        this.geminiWebClient = geminiWebClient;
        this.aiExplanationRepository = aiExplanationRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AiExplanationDto explainEvent(ExplainEventRequest request) {
        String cacheKey = "event:" + request.symbol() + ":" + request.eventDate();
        Optional<AiExplanation> cached = aiExplanationRepository.findByCacheKey(cacheKey);
        if (cached.isPresent()) {
            return parseStoredResponse(cached.get().getResponse(), true);
        }

        String prompt = buildEventPrompt(request);
        String response = callGemini(prompt);
        if (response == null) {
            return fallbackExplanation();
        }

        saveExplanation(cacheKey, AiExplanation.ExplanationType.EVENT, prompt, response);
        return parseStoredResponse(response, false);
    }

    @Transactional
    public AiExplanationDto explainTerm(ExplainTermRequest request) {
        String cacheKey = "term:" + request.term().toLowerCase().trim();
        Optional<AiExplanation> cached = aiExplanationRepository.findByCacheKey(cacheKey);
        if (cached.isPresent()) {
            return parseStoredResponse(cached.get().getResponse(), true);
        }

        String prompt = buildTermPrompt(request.term());
        String response = callGemini(prompt);
        if (response == null) {
            return fallbackExplanation();
        }

        saveExplanation(cacheKey, AiExplanation.ExplanationType.TERM, prompt, response);
        return parseStoredResponse(response, false);
    }

    @Transactional
    public AiExplanationDto explainStock(ExplainStockRequest request) {
        String question = request.question() != null ? request.question() : "Bu şirketi basitçe anlat";
        String cacheKey = "stock:" + request.symbol() + ":" + question.toLowerCase().hashCode();
        Optional<AiExplanation> cached = aiExplanationRepository.findByCacheKey(cacheKey);
        if (cached.isPresent()) {
            return parseStoredResponse(cached.get().getResponse(), true);
        }

        String prompt = buildStockPrompt(request, question);
        String response = callGemini(prompt);
        if (response == null) {
            return fallbackExplanation();
        }

        saveExplanation(cacheKey, AiExplanation.ExplanationType.STOCK, prompt, response);
        return parseStoredResponse(response, false);
    }

    private String callGemini(String prompt) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            log.warn("Gemini API key not configured, returning null");
            return null;
        }
        try {
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                    "parts", List.of(Map.of("text", prompt))
                ))
            );

            String raw = geminiWebClient.post()
                    .uri("?key=" + geminiApiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(raw);
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            return null;
        }
    }

    private AiExplanationDto parseStoredResponse(String rawText, boolean cached) {
        if (rawText == null || rawText.isBlank()) {
            return fallbackExplanation();
        }
        String[] lines = rawText.split("\n");
        String summary = lines.length > 0 ? lines[0].replaceAll("^\\*+|\\*+$|^\\d+\\.\\s*", "").trim() : rawText.substring(0, Math.min(200, rawText.length()));
        List<String> factors = List.of(
            "Bu gelişmeler fiyat hareketine katkı sağlamış olabilir.",
            "Piyasa beklentileri ve genel ekonomik koşullar da etkili olmuş olabilir."
        );
        String learningNote = "Hisseleri etkileyen tek bir faktör olmayabilir. Farklı açılardan değerlendirmek önemlidir.";
        return new AiExplanationDto(summary, factors, learningNote, AI_DISCLAIMER, cached);
    }

    private AiExplanationDto fallbackExplanation() {
        return new AiExplanationDto(
            "Yapay zekâ açıklaması şu anda kullanılamıyor. Ders merkezindeki eğitim içeriklerini inceleyebilirsiniz.",
            List.of(
                "Fiyat hareketleri birden fazla faktörden etkilenebilir.",
                "Şirket haberleri, sektör gelişmeleri ve genel piyasa koşulları rol oynayabilir."
            ),
            "Bir hisseyi anlamak için şirketin ne iş yaptığını, finansal verilerini ve sektör bağlamını birlikte değerlendirin.",
            AI_DISCLAIMER,
            false
        );
    }

    private void saveExplanation(String cacheKey, AiExplanation.ExplanationType type, String prompt, String response) {
        AiExplanation explanation = new AiExplanation();
        explanation.setCacheKey(cacheKey);
        explanation.setExplanationType(type);
        explanation.setPromptSummary(prompt.substring(0, Math.min(500, prompt.length())));
        explanation.setResponse(response);
        aiExplanationRepository.save(explanation);
    }

    private String buildEventPrompt(ExplainEventRequest req) {
        return """
            Sen bir yatırım danışmanı değilsin.
            Yeni başlayan kullanıcılara borsayı öğretmek için sade açıklamalar yapan bir finansal okuryazarlık asistanısın.

            Kurallar:
            - Kesin neden-sonuç ilişkisi kurma.
            - Al/sat önerisi verme.
            - Fiyat tahmini yapma.
            - "Olabilir", "katkı sağlamış olabilir", "dikkat çekiyor" gibi ihtiyatlı ifadeler kullan.
            - Cevabı kısa, sade ve eğitim odaklı ver.

            Hisse: %s
            Şirket: %s
            Tarih: %s
            Fiyat değişimi: %.2f%%
            Olay: %s
            İlgili haberler: %s

            Lütfen kısa, anlaşılır ve eğitim odaklı bir açıklama yap. Yatırım tavsiyesi verme.
            """.formatted(
                req.symbol(), req.companyName(), req.eventDate(),
                req.priceChangePercent() != null ? req.priceChangePercent().doubleValue() : 0.0,
                req.eventTitle(),
                req.relatedNews() != null ? String.join("; ", req.relatedNews()) : "Bilgi yok"
            );
    }

    private String buildTermPrompt(String term) {
        return """
            Sen bir finansal okuryazarlık asistanısın. Yatırım tavsiyesi vermiyorsun.

            "%s" terimini yeni başlayan bir yatırımcıya 2-3 cümleyle sade Türkçe ile açıkla.
            Teknik jargondan kaçın. Gerçek bir örnek ver.
            Kesinlikle al/sat tavsiyesi verme.
            """.formatted(term);
    }

    private String buildStockPrompt(ExplainStockRequest req, String question) {
        return """
            Sen bir finansal okuryazarlık asistanısın. Yatırım tavsiyesi vermiyorsun.

            Hisse: %s (%s)
            Sektör: %s

            Kullanıcı sorusu: %s

            Kısa, sade, eğitim odaklı cevap ver. Al/sat önerisi verme. Kesin tahmin yapma.
            """.formatted(req.symbol(), req.companyName(), req.sector() != null ? req.sector() : "Bilinmiyor", question);
    }
}
