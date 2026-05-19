package com.basitborsa.service;

import com.basitborsa.client.PythonAiClient;
import com.basitborsa.config.ActiveSymbolsConfig;
import com.basitborsa.dto.ai.*;
import com.basitborsa.entity.AiExplanation;
import com.basitborsa.entity.Stock;
import com.basitborsa.exception.ResourceNotFoundException;
import com.basitborsa.repository.AiExplanationRepository;
import com.basitborsa.repository.StockRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.basitborsa.util.AppConstants.AI_DISCLAIMER;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final AiExplanationRepository aiExplanationRepository;
    private final ObjectMapper objectMapper;
    private final PythonAiClient pythonAiClient;
    private final AiContextBuilder aiContextBuilder;
    private final StockRepository stockRepository;
    private final ActiveSymbolsConfig activeSymbols;
    private final ObjectProvider<MarketDataSyncService> syncServiceProvider;

    public AiService(AiExplanationRepository aiExplanationRepository,
                     ObjectMapper objectMapper,
                     PythonAiClient pythonAiClient,
                     AiContextBuilder aiContextBuilder,
                     StockRepository stockRepository,
                     ActiveSymbolsConfig activeSymbols,
                     ObjectProvider<MarketDataSyncService> syncServiceProvider) {
        this.aiExplanationRepository = aiExplanationRepository;
        this.objectMapper = objectMapper;
        this.pythonAiClient = pythonAiClient;
        this.aiContextBuilder = aiContextBuilder;
        this.stockRepository = stockRepository;
        this.activeSymbols = activeSymbols;
        this.syncServiceProvider = syncServiceProvider;
    }

    @Transactional
    public AiExplanationDto explainEvent(ExplainEventRequest request) {
        String cacheKey = "event:" + request.symbol() + ":" + request.eventDate();
        Optional<AiExplanation> cached = aiExplanationRepository.findByCacheKey(cacheKey);
        if (cached.isPresent()) {
            try {
                AiExplanationDto stored = objectMapper.readValue(cached.get().getResponse(), AiExplanationDto.class);
                return new AiExplanationDto(stored.summary(), stored.possibleFactors(),
                        stored.learningNote(), stored.disclaimer(), true);
            } catch (Exception ignored) { /* refetch */ }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("symbol", request.symbol());
        payload.put("companyName", request.companyName());
        payload.put("sector", "");
        payload.put("eventDate", request.eventDate() != null ? request.eventDate().toString() : "");
        payload.put("priceChangePercent", request.priceChangePercent() != null
                ? request.priceChangePercent().doubleValue() : 0.0);
        payload.put("eventTitle", request.eventTitle());
        payload.put("relatedNews", request.relatedNews() != null ? request.relatedNews() : List.of());

        AiExplanationDto dto = pythonAiClient.explainEvent(payload);
        persistCache(cacheKey, AiExplanation.ExplanationType.EVENT, dto);
        return dto;
    }

    @Transactional
    public AiExplanationDto explainTerm(ExplainTermRequest request) {
        String cacheKey = "term:" + request.term().toLowerCase().trim();
        Optional<AiExplanation> cached = aiExplanationRepository.findByCacheKey(cacheKey);
        if (cached.isPresent()) {
            try {
                AiExplanationDto stored = objectMapper.readValue(cached.get().getResponse(), AiExplanationDto.class);
                return new AiExplanationDto(stored.summary(), stored.possibleFactors(),
                        stored.learningNote(), stored.disclaimer(), true);
            } catch (Exception ignored) { /* refetch */ }
        }
        AiExplanationDto dto = pythonAiClient.explainTerm(request.term());
        persistCache(cacheKey, AiExplanation.ExplanationType.TERM, dto);
        return dto;
    }

    @Transactional
    public AiExplanationDto explainStock(ExplainStockRequest request) {
        String question = request.question() != null ? request.question() : "Bu şirketi basitçe anlat";
        String cacheKey = "stock:" + request.symbol() + ":" + question.toLowerCase().hashCode();
        Optional<AiExplanation> cached = aiExplanationRepository.findByCacheKey(cacheKey);
        if (cached.isPresent()) {
            try {
                AiExplanationDto stored = objectMapper.readValue(cached.get().getResponse(), AiExplanationDto.class);
                return new AiExplanationDto(stored.summary(), stored.possibleFactors(),
                        stored.learningNote(), stored.disclaimer(), true);
            } catch (Exception ignored) { /* refetch */ }
        }
        AiExplanationDto dto = pythonAiClient.explainStock(
                request.symbol(), request.companyName(), request.sector(), question);
        persistCache(cacheKey, AiExplanation.ExplanationType.STOCK, dto);
        return dto;
    }

    @Transactional(readOnly = true)
    public ChartStoryResponse chartStory(ChartStoryRequest request) {
        Stock stock = stockRepository.findBySymbol(request.symbol().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + request.symbol()));

        if (!activeSymbols.isMarketActive(stock.getSymbol())) {
            return demoLimitedResponse(stock.getSymbol());
        }

        Map<String, Object> context = aiContextBuilder.buildChartStoryContext(stock, request.date());
        if (context == null) {
            // No real market data — trigger sync and return unavailable
            triggerSync(stock.getSymbol());
            return unavailableResponse(stock.getSymbol());
        }
        return pythonAiClient.chartStory(context);
    }

    private ChartStoryResponse demoLimitedResponse(String symbol) {
        return new ChartStoryResponse(
                ActiveSymbolsConfig.DEMO_LIMITED_CHART_STORY,
                List.of(
                        new ChartStorySection("Demo kapsamı",
                                "Bu hackathon demosunda gerçek gecikmeli/gün sonu veri ve " +
                                "grafik hikâyesi yalnızca THYAO için aktiftir. " + symbol +
                                " kartı eğitim kapsamını göstermek için listelenmiştir."),
                        new ChartStorySection("Ne yapabilirim?",
                                "THYAO hissesini açarak gerçek veri üzerinden grafik hikâyesini " +
                                "deneyimleyebilir, diğer şirketler için Öğrenme Merkezi'ndeki " +
                                "eğitim içeriklerine göz atabilirsiniz.")
                ),
                List.of(AI_DISCLAIMER, "Gerçek dışı/sahte fiyat verisi gösterilmez."),
                "DEMO_LIMITED",
                true);
    }

    private ChartStoryResponse unavailableResponse(String symbol) {
        return new ChartStoryResponse(
                symbol + " için şu an gerçek piyasa verisi mevcut değil.",
                List.of(
                        new ChartStorySection("Veri durumu",
                                "Sağlayıcıdan gelen gün sonu fiyat verisi henüz hazır değil. " +
                                "Veriler senkronize edildiğinde grafik açıklaması aktif olur."),
                        new ChartStorySection("Ne yapabilirim?",
                                "Birkaç saniye sonra sayfayı yenileyebilir veya başka bir hisseyi seçebilirsiniz.")
                ),
                List.of(AI_DISCLAIMER, "Gerçek dışı/sahte fiyat verisi gösterilmez."),
                "UNAVAILABLE",
                true);
    }

    private void triggerSync(String symbol) {
        MarketDataSyncService svc = syncServiceProvider.getIfAvailable();
        if (svc == null) return;
        try { svc.requestAsyncSync(symbol, "chart-story-no-real-data"); } catch (Exception ignored) {}
    }

    private void persistCache(String cacheKey, AiExplanation.ExplanationType type, AiExplanationDto dto) {
        try {
            AiExplanation explanation = new AiExplanation();
            explanation.setCacheKey(cacheKey);
            explanation.setExplanationType(type);
            explanation.setPromptSummary(cacheKey.substring(0, Math.min(500, cacheKey.length())));
            explanation.setResponse(objectMapper.writeValueAsString(dto));
            aiExplanationRepository.save(explanation);
        } catch (Exception e) {
            log.warn("Cache persist failed for {}: {}", cacheKey, e.getMessage());
        }
    }
}
