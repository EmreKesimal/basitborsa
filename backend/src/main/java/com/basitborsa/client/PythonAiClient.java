package com.basitborsa.client;

import com.basitborsa.dto.ai.AiExplanationDto;
import com.basitborsa.dto.ai.ChartStoryResponse;
import com.basitborsa.dto.ai.ChartStorySection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.basitborsa.util.AppConstants.AI_DISCLAIMER;

@Component
public class PythonAiClient {

    private static final Logger log = LoggerFactory.getLogger(PythonAiClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.service.internal-api-key:local-dev-secret}")
    private String internalApiKey;

    public PythonAiClient(@Qualifier("pythonAiWebClient") WebClient webClient,
                          ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public ChartStoryResponse chartStory(Map<String, Object> context) {
        try {
            return webClient.post()
                    .uri("/ai/chart-story")
                    .header("X-Internal-Api-Key", internalApiKey)
                    .bodyValue(context)
                    .retrieve()
                    .bodyToMono(ChartStoryResponse.class)
                    .timeout(TIMEOUT)
                    .block();
        } catch (Exception e) {
            log.warn("Python AI service unavailable for chart-story: {}", e.getMessage());
            return fallbackChartStory(context);
        }
    }

    public AiExplanationDto explainTerm(String term) {
        try {
            String body = webClient.post()
                    .uri("/ai/explain-term")
                    .header("X-Internal-Api-Key", internalApiKey)
                    .bodyValue(Map.of("term", term, "userLevel", "beginner", "language", "tr"))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(TIMEOUT)
                    .block();
            JsonNode node = objectMapper.readTree(body);
            String simple = node.path("simpleExplanation").asText("");
            String why = node.path("whyItMatters").asText("");
            String example = node.path("example").isNull() ? null : node.path("example").asText(null);
            String warning = node.path("warning").asText(AI_DISCLAIMER);

            List<String> factors = new ArrayList<>();
            if (example != null && !example.isBlank()) factors.add("Örnek: " + example);
            if (!why.isBlank()) factors.add(why);
            if (factors.isEmpty()) factors.add("Bu kavram için ek bilgi henüz hazır değil.");

            return new AiExplanationDto(simple.isBlank() ? warning : simple, factors,
                    "Tek bir kavram bir yatırım kararı için yeterli olmayabilir; bağlamla birlikte değerlendirin.",
                    AI_DISCLAIMER, false);
        } catch (Exception e) {
            log.warn("Python AI service unavailable for explain-term: {}", e.getMessage());
            return fallbackExplanation();
        }
    }

    public AiExplanationDto explainStock(String symbol, String companyName, String sector, String question) {
        try {
            String body = webClient.post()
                    .uri("/ai/explain-stock")
                    .header("X-Internal-Api-Key", internalApiKey)
                    .bodyValue(Map.of(
                            "symbol", symbol,
                            "companyName", companyName != null ? companyName : symbol,
                            "sector", sector != null ? sector : "",
                            "question", question != null ? question : "Bu şirketi basitçe anlat",
                            "userLevel", "beginner",
                            "language", "tr"
                    ))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(TIMEOUT)
                    .block();
            return parseExplanation(body);
        } catch (Exception e) {
            log.warn("Python AI service unavailable for explain-stock: {}", e.getMessage());
            return fallbackExplanation();
        }
    }

    public AiExplanationDto explainEvent(Map<String, Object> payload) {
        try {
            String body = webClient.post()
                    .uri("/ai/explain-event")
                    .header("X-Internal-Api-Key", internalApiKey)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(TIMEOUT)
                    .block();
            return parseExplanation(body);
        } catch (Exception e) {
            log.warn("Python AI service unavailable for explain-event: {}", e.getMessage());
            return fallbackExplanation();
        }
    }

    private AiExplanationDto parseExplanation(String body) throws Exception {
        JsonNode node = objectMapper.readTree(body);
        String summary = node.path("summary").asText("");
        List<String> factors = new ArrayList<>();
        if (node.path("possibleFactors").isArray()) {
            node.path("possibleFactors").forEach(n -> factors.add(n.asText()));
        }
        if (factors.isEmpty()) {
            factors.add("Fiyat hareketleri birden fazla faktörden etkilenebilir.");
        }
        String learningNote = node.path("learningNote").asText(
                "Hisseleri etkileyen tek bir faktör olmayabilir; bağlamla birlikte değerlendirilmelidir.");
        String disclaimer = node.path("disclaimer").asText(AI_DISCLAIMER);
        return new AiExplanationDto(
                summary.isBlank() ? "Bu konu için kısa bir açıklama hazırlandı." : summary,
                factors, learningNote, disclaimer, false);
    }

    private AiExplanationDto fallbackExplanation() {
        return new AiExplanationDto(
                "Yapay zekâ açıklaması şu anda kullanılamıyor. Ders merkezindeki eğitim içeriklerini inceleyebilirsiniz.",
                List.of(
                        "Fiyat hareketleri birden fazla faktörden etkilenebilir.",
                        "Şirket haberleri, sektör gelişmeleri ve genel piyasa koşulları yatırımcı algısını etkilemiş olabilir."
                ),
                "Bir hisseyi anlamak için şirketin ne iş yaptığını, finansal verilerini ve sektör bağlamını birlikte değerlendirin.",
                AI_DISCLAIMER, false);
    }

    private ChartStoryResponse fallbackChartStory(Map<String, Object> context) {
        String symbol = context.getOrDefault("symbol", "").toString();
        return new ChartStoryResponse(
            symbol + " için seçilen dönemde dikkat çeken bir fiyat hareketi görülüyor.",
            List.of(
                new ChartStorySection("Grafikte ne oldu?",
                    "Bu dönemde fiyat hareketleri dikkat çekiyor. Detaylı analiz için yapay zekâ servisi şu an kullanılamıyor."),
                new ChartStorySection("Aynı dönemde hangi gelişmeler vardı?",
                    "İlgili haberler ve sektör gelişmeleri bu dönemde yatırımcı algısını etkilemiş olabilir."),
                new ChartStorySection("Bu gelişmeler fiyat hareketiyle nasıl ilişkili olabilir?",
                    "Bu gelişmeler fiyat hareketine katkı sağlamış olabilir, ancak fiyat hareketlerinin tek ve kesin nedeni olmayabilir."),
                new ChartStorySection("Yeni başlayan biri buradan ne öğrenmeli?",
                    "Hisse fiyatları şirket haberleri, sektör beklentileri ve genel piyasa koşullarıyla birlikte değerlendirilmelidir.")
            ),
            List.of(AI_DISCLAIMER),
            "fallback",
            true
        );
    }
}
