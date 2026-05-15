package com.basitborsa.client;

import com.basitborsa.dto.ai.ChartStoryResponse;
import com.basitborsa.dto.ai.ChartStorySection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class PythonAiClient {

    private static final Logger log = LoggerFactory.getLogger(PythonAiClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final WebClient webClient;

    @Value("${ai.service.internal-api-key:local-dev-secret}")
    private String internalApiKey;

    public PythonAiClient(@Qualifier("pythonAiWebClient") WebClient webClient) {
        this.webClient = webClient;
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
            List.of("Bu açıklama yatırım tavsiyesi değildir."),
            "fallback",
            true
        );
    }
}
