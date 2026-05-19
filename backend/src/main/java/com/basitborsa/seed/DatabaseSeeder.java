package com.basitborsa.seed;

import com.basitborsa.entity.*;
import com.basitborsa.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Seeds only non-price metadata: users, stock identity (symbol/company/sector/description),
 * educational lessons, and labeled educational event markers.
 *
 * Does NOT seed OHLCV prices, latest prices, news, or any data that must originate from a
 * real market/news provider. Real prices come from MarketDataSyncService. Real news comes
 * from NewsSyncService.
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final StockEventRepository stockEventRepository;
    private final LessonRepository lessonRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockNewsRepository stockNewsRepository;

    public DatabaseSeeder(UserRepository userRepository,
                          StockRepository stockRepository,
                          StockEventRepository stockEventRepository,
                          LessonRepository lessonRepository,
                          StockPriceRepository stockPriceRepository,
                          StockNewsRepository stockNewsRepository) {
        this.userRepository = userRepository;
        this.stockRepository = stockRepository;
        this.stockEventRepository = stockEventRepository;
        this.lessonRepository = lessonRepository;
        this.stockPriceRepository = stockPriceRepository;
        this.stockNewsRepository = stockNewsRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUser();
        seedStocks();
        seedLessons();
        purgeLegacySeedData();
        log.info("Seed data loaded (metadata only — no prices, no news).");
    }

    /**
     * One-time cleanup: remove any legacy SEED-sourced prices/news + null out hardcoded
     * Stock price/ratio fields so the UI shows UNAVAILABLE until real provider sync runs.
     * Safe to run on every boot — idempotent.
     */
    private void purgeLegacySeedData() {
        // Drop SEED news entirely
        int newsDropped = 0;
        for (var n : new java.util.ArrayList<>(stockNewsRepository.findAll())) {
            if (n.getSourceType() == null || "SEED".equalsIgnoreCase(n.getSourceType())) {
                stockNewsRepository.delete(n);
                newsDropped++;
            }
        }
        // Drop SEED prices
        int pricesDropped = 0;
        for (var p : new java.util.ArrayList<>(stockPriceRepository.findAll())) {
            if (p.getDataSource() == null || "SEED".equalsIgnoreCase(p.getDataSource())
                    || "FALLBACK".equalsIgnoreCase(p.getDataSource())
                    || "SEED_FALLBACK".equalsIgnoreCase(p.getDataSource())) {
                stockPriceRepository.delete(p);
                pricesDropped++;
            }
        }
        // Null out legacy hardcoded stock fields if they still carry SEED markers
        int stocksReset = 0;
        for (var s : stockRepository.findAll()) {
            if (s.getDataSource() == null || "SEED".equalsIgnoreCase(s.getDataSource())) {
                s.setCurrentPrice(null);
                s.setDailyChangePercent(null);
                s.setPeRatio(null);
                s.setPbRatio(null);
                s.setDividendYield(null);
                s.setMarketCapBillions(null);
                s.setDataSource(null);
                s.setFallback(false);
                stockRepository.save(s);
                stocksReset++;
            }
        }
        if (newsDropped + pricesDropped + stocksReset > 0) {
            log.info("Legacy SEED purge: news={} prices={} stocks-reset={}",
                    newsDropped, pricesDropped, stocksReset);
        }
    }

    private void seedUser() {
        if (userRepository.findByUsername("demo").isEmpty()) {
            User demo = new User("demo", "demo@basitborsa.com");
            userRepository.save(demo);
            log.info("Demo user created.");
        }
    }

    private void seedStocks() {
        ensureStock("THYAO", "Türk Hava Yolları A.O.", "Havacılık & Ulaşım",
                "Türkiye'nin bayrak taşıyıcı havayolu şirketi. Yolcu ve kargo taşımacılığı alanında faaliyet göstermektedir.",
                List.of(
                        edu(20, "Güçlü yolcu artışı", StockEvent.EventType.RISE,
                                "Havacılık hisseleri yolcu sayısı, yakıt maliyeti, döviz kuru ve sektör beklentilerinden etkilenebilir."),
                        edu(12, "Bilanço dönemi öğrenme notu", StockEvent.EventType.NEUTRAL,
                                "Bilanço açıklamaları hisse fiyatlarını kısa vadede önemli ölçüde etkileyebilir."),
                        edu(5,  "Yakıt maliyeti öğrenme notu", StockEvent.EventType.FALL,
                                "Havayolu şirketleri yakıt maliyetlerine karşı kırılgan olabilir; petrol fiyatları izlenmesi gereken bir göstergedir.")
                ));

        ensureStock("ASELS", "Aselsan Elektronik San. A.Ş.", "Savunma & Teknoloji",
                "Türkiye'nin önde gelen savunma elektroniği şirketi.",
                List.of(
                        edu(18, "Savunma sözleşmesi öğrenme notu", StockEvent.EventType.RISE,
                                "Savunma şirketleri için sözleşme haberleri önemli bir katalizör olabilir."),
                        edu(10, "Ar-Ge yatırımı öğrenme notu", StockEvent.EventType.NEUTRAL,
                                "Ar-Ge harcamaları kısa vadede maliyeti artırabilir, uzun vadede büyümeye katkı sağlayabilir.")
                ));

        ensureStock("BIMAS", "BİM Birleşik Mağazalar A.Ş.", "Perakende & Gıda",
                "Türkiye'nin büyük indirim market zincirlerinden biri.",
                List.of(
                        edu(22, "Mağaza büyümesi öğrenme notu", StockEvent.EventType.RISE,
                                "Perakende sektöründe büyüme genellikle mağaza sayısı ve same-store satış artışıyla ölçülür.")
                ));

        ensureStock("SISE", "Türkiye Şişe ve Cam Fabrikaları A.Ş.", "Cam & Sanayi",
                "Türkiye'nin önde gelen cam üreticilerinden biri.",
                List.of(
                        edu(15, "Enerji maliyeti öğrenme notu", StockEvent.EventType.FALL,
                                "Enerji yoğun sektörlerde enerji fiyatları karlılık üzerinde belirleyici rol oynayabilir.")
                ));

        ensureStock("TUPRS", "Tüpraş-Türkiye Petrol Rafinerileri A.Ş.", "Enerji & Petrokimya",
                "Türkiye'nin tek entegre petrol rafinerisi.",
                List.of(
                        edu(19, "Rafineri marjı öğrenme notu", StockEvent.EventType.RISE,
                                "Rafineri şirketleri ham petrol ve ürün fiyatları arasındaki marjdan gelir elde eder.")
                ));

        ensureStock("KCHOL", "Koç Holding A.Ş.", "Holding & Sanayi",
                "Türkiye'nin en büyük holding şirketlerinden biri.",
                List.of(
                        edu(21, "Holding değerleme öğrenme notu", StockEvent.EventType.NEUTRAL,
                                "Holding hisseleri bünyesindeki iştiraklerin performansından etkilenebilir.")
                ));

        ensureStock("GARAN", "Türkiye Garanti Bankası A.Ş.", "Bankacılık",
                "Türkiye'nin önde gelen özel bankalarından biri.",
                List.of(
                        edu(24, "Bankacılık kârlılığı öğrenme notu", StockEvent.EventType.RISE,
                                "Bankacılık hisseleri faiz marjı, kredi büyümesi ve takipteki krediler gibi göstergelerden etkilenebilir.")
                ));

        ensureStock("FROTO", "Ford Otomotiv Sanayi A.Ş.", "Otomotiv & Sanayi",
                "Türkiye'nin önde gelen ticari araç üreticilerinden biri.",
                List.of(
                        edu(23, "İhracat ve döviz öğrenme notu", StockEvent.EventType.RISE,
                                "İhracat ağırlıklı şirketler için Avrupa talebi ve döviz kuru önemli faktörler olabilir.")
                ));

        log.info("Stock metadata seed checked/loaded.");
    }

    private void ensureStock(String symbol, String company, String sector, String description,
                             List<EduEvent> events) {
        if (stockRepository.existsBySymbol(symbol)) return;
        Stock stock = new Stock();
        stock.setSymbol(symbol);
        stock.setCompanyName(company);
        stock.setSector(sector);
        stock.setDescription(description);
        // Prices, ratios and market cap intentionally null — populated only by real provider sync.
        stock.setDataSource(null);
        stock.setFallback(false);
        stock = stockRepository.save(stock);

        for (EduEvent e : events) {
            StockEvent event = new StockEvent();
            event.setStock(stock);
            event.setEventDate(LocalDate.now().minusDays(e.daysAgo));
            event.setTitle(e.title);
            event.setEventType(e.type);
            event.setPriceChangePercent(BigDecimal.ZERO);
            event.setShortDescription("Eğitsel öğrenme notu — gerçek haber değildir.");
            event.setRelatedNews(null);
            event.setLearningNote(e.learningNote);
            stockEventRepository.save(event);
        }
    }

    private EduEvent edu(int daysAgo, String title, StockEvent.EventType type, String note) {
        return new EduEvent(daysAgo, title, type, note);
    }

    private record EduEvent(int daysAgo, String title, StockEvent.EventType type, String learningNote) {}

    private void seedLessons() {
        if (lessonRepository.count() > 0) return;

        List<LessonData> lessons = List.of(
            new LessonData("Borsa Nedir?", "borsa-nedir", "storefront", "primary-container",
                "Alıcıların ve satıcıların hisse senetleri gibi finansal ürünleri alıp sattığı organize bir pazar yeridir.",
                "Borsa, şirketlerin halka açılarak sermaye toplamasına ve yatırımcıların bu şirketlere ortak olmasına olanak tanır. Türkiye'de Borsa İstanbul (BIST) bu işlevi üstlenmektedir.",
                "Borsa fiyatları arz ve talebe göre her an değişebilir. Yatırım yapmadan önce iyi araştırma yapmak önemlidir.",
                "Bir şirketin hissesini aldığınızda o şirketin küçük bir ortağı olursunuz.",
                "Neden önemli? Tasarruflarınızı büyütmek için borsayı anlamak ilk adımdır.", 1),
            new LessonData("Hisse Nedir?", "hisse-nedir", "pie_chart", "tertiary-container",
                "Bir şirketin sahipliğinin küçük bir parçasıdır.",
                "Şirketler büyüme sermayesi toplamak için halka arz (IPO) yaparak hisselerini borsada satışa sunar.",
                "Hisse fiyatı şirketin değeri, beklentiler ve piyasa koşullarına göre sürekli değişir.",
                "100 TL'ye THYAO hissesi aldığınızda Türk Hava Yolları'nın küçük bir ortağı olursunuz.",
                "Neden önemli? Hissenin ne olduğunu anlamadan borsa yatırımı yapmak risklidir.", 2),
            new LessonData("F/K Oranı Nedir?", "fk-orani-nedir", "calculate", "secondary-container",
                "Fiyat/Kazanç oranı. Hisse fiyatının hisse başına düşen kâra oranıdır.",
                "F/K, 1 birim kazanç için kaç birim ödemeye razı olunduğunu gösterir.",
                "F/K tek başına yeterli değildir; sektör ortalaması ve büyüme beklentisiyle değerlendirilir.",
                "F/K=10 → şirketin mevcut karını 10 yılda çıkaracağı anlamına gelir.",
                "Neden önemli? Pahalı/ucuz değerlendirmesinde ilk göstergelerdendir.", 3),
            new LessonData("PD/DD Nedir?", "pd-dd-nedir", "balance", "primary-container",
                "Piyasa Değeri / Defter Değeri oranı.",
                "PD/DD<1 → defter değerinin altında işlem görüyor olabilir.",
                "Bankacılık/finansta daha anlamlı; teknolojide yüksek olabilir.",
                "Varlık 100 TL, piyasa değeri 80 TL → PD/DD=0.8.",
                "Neden önemli? Piyasanın biçtiği değeri anlamak için kullanılır.", 4),
            new LessonData("Temettü Nedir?", "temettu-nedir", "payments", "primary-container",
                "Şirket kârının bir kısmının nakit olarak hissedarlara dağıtılmasıdır.",
                "Şirketler kârı yeniden yatırabilir ya da hissedarlara temettü olarak dağıtabilir.",
                "Yüksek temettü her zaman iyi yatırım anlamına gelmez; sürdürülebilirlik önemlidir.",
                "1000 hisse, hisse başı 5 TL temettü → 5000 TL nakit.",
                "Neden önemli? Pasif gelir için kriter olabilir.", 5),
            new LessonData("Volatilite Nedir?", "volatilite-nedir", "trending_up", "primary-container",
                "Fiyatın ne kadar hızlı ve büyük değiştiğinin ölçüsüdür.",
                "Yüksek volatilite → kısa sürede büyük yüzdelik değişimler.",
                "Yüksek volatilite hem yüksek kazanç hem yüksek kayıp anlamına gelebilir.",
                "Bir hisse bir gün %5 artıp ertesi gün %4 düşüyorsa volatilite yüksektir.",
                "Neden önemli? Risk toleransına uygun hisse seçimi.", 6),
            new LessonData("Risk Nedir?", "risk-nedir", "warning", "tertiary-container",
                "Yatırımın beklenenden farklı performans göstermesi ihtimalidir.",
                "Her yatırım risk taşır; çeşitlendirme riski dağıtır.",
                "Yüksek getiri çoğunlukla yüksek riskle gelir.",
                "Tek hisse yerine farklı sektörlerden hisse almak riski azaltır.",
                "Neden önemli? Riski anlamadan yatırım kayba yol açabilir.", 7),
            new LessonData("Grafik Nasıl Okunur?", "grafik-nasil-okunur", "show_chart", "primary-container",
                "Fiyat grafikleri zamanla fiyatın nasıl değiştiğini görselleştirir.",
                "Çizgi grafiklerde yatay eksen zaman, dikey eksen fiyattır.",
                "Geçmiş performans gelecek performansı garantilemez.",
                "Sol uçtaki fiyatla sağ uçtakini karşılaştırıp yüzde değişim hesaplanabilir.",
                "Neden önemli? Fiyat hareketini anlamak temel beceridir.", 8),
            new LessonData("Haberler Fiyatı Nasıl Etkiler?", "haberler-fiyat-etkisi", "newspaper", "secondary-container",
                "Şirket/sektör/ekonomi haberleri yatırımcı kararlarını etkileyebilir.",
                "Olumlu haberler fiyatı yukarı, olumsuzlar aşağı çekebilir; piyasa beklenmedik tepki de verebilir.",
                "Bir haberin etkisi tahmin edilemeyebilir; piyasa beklenen haberi önceden fiyatlayabilir.",
                "Yolcu rekoru haberi havacılık hisselerini yukarı çekebilir.",
                "Neden önemli? Haberleri takip etmek bilinçli karar için yardımcıdır.", 9),
            new LessonData("Bilanço Nedir?", "bilanco-nedir", "description", "primary-container",
                "Bir şirketin belirli andaki varlık, borç ve özkaynak tablosudur.",
                "Varlıklar = Borçlar + Özkaynaklar.",
                "Bilanço dönemsel ve sektör ortalamasıyla karşılaştırmalı yorumlanmalıdır.",
                "100 TL varlık, 60 TL borç → 40 TL özkaynak.",
                "Neden önemli? Finansal sağlığı okumanın anahtarıdır.", 10)
        );

        int order = 1;
        for (LessonData data : lessons) {
            Lesson lesson = new Lesson();
            lesson.setTitle(data.title());
            lesson.setSlug(data.slug());
            lesson.setIconName(data.icon());
            lesson.setAccentColor(data.accentColor());
            lesson.setShortDescription(data.shortDesc());
            lesson.setContent(data.content());
            lesson.setBeginnerWarning(data.warning());
            lesson.setExampleText(data.example());
            lesson.setWhyItMatters(data.whyItMatters());
            lesson.setDifficultyLevel(Lesson.DifficultyLevel.BEGINNER);
            lesson.setSortOrder(order++);
            lessonRepository.save(lesson);
        }
        log.info("Lesson seed data loaded.");
    }

    private record LessonData(String title, String slug, String icon, String accentColor,
                              String shortDesc, String content, String warning, String example,
                              String whyItMatters, int sortOrder) {}
}
