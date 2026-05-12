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

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockEventRepository stockEventRepository;
    private final PortfolioRepository portfolioRepository;
    private final LessonRepository lessonRepository;

    public DatabaseSeeder(UserRepository userRepository,
                          StockRepository stockRepository,
                          StockPriceRepository stockPriceRepository,
                          StockEventRepository stockEventRepository,
                          PortfolioRepository portfolioRepository,
                          LessonRepository lessonRepository) {
        this.userRepository = userRepository;
        this.stockRepository = stockRepository;
        this.stockPriceRepository = stockPriceRepository;
        this.stockEventRepository = stockEventRepository;
        this.portfolioRepository = portfolioRepository;
        this.lessonRepository = lessonRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUser();
        seedStocks();
        seedLessons();
        log.info("Seed data loaded successfully.");
    }

    private void seedUser() {
        if (userRepository.findByUsername("demo").isEmpty()) {
            User demo = new User("demo", "demo@basitborsa.com");
            userRepository.save(demo);
            log.info("Demo user created.");
        }
    }

    private void seedStocks() {
        if (stockRepository.count() > 0) return;

        seedTHYAO();
        seedASELS();
        seedBIMAS();
        seedSISE();
        seedTUPRS();
        log.info("Stock seed data loaded.");
    }

    private void seedTHYAO() {
        Stock stock = new Stock();
        stock.setSymbol("THYAO");
        stock.setCompanyName("Türk Hava Yolları A.O.");
        stock.setSector("Havacılık & Ulaşım");
        stock.setDescription("Türkiye'nin bayrak taşıyıcı havayolu şirketi. Dünyanın en çok ülkesine uçan havayollarından biri. Yolcu ve kargo taşımacılığı alanında faaliyet göstermektedir.");
        stock.setCurrentPrice(new BigDecimal("305.25"));
        stock.setDailyChangePercent(new BigDecimal("2.45"));
        stock.setPeRatio(new BigDecimal("4.2"));
        stock.setPbRatio(new BigDecimal("1.1"));
        stock.setDividendYield(new BigDecimal("0.00"));
        stock.setMarketCapBillions(new BigDecimal("420.50"));
        stock.setDataSource("SEED");
        stock.setFallback(true);
        stock = stockRepository.save(stock);

        seedPrices(stock, new BigDecimal("275.00"), 30, 8.5);
        seedEvents(stock, List.of(
            new EventData(LocalDate.now().minusDays(20), "Güçlü yolcu artışı", StockEvent.EventType.RISE,
                new BigDecimal("5.9"), "Yolcu sayılarında belirgin artış görüldü.",
                "Yolcu sayılarında artış açıklandı; havacılık sektöründe olumlu beklentiler güçlendi.",
                "Havacılık hisseleri yolcu sayısı, yakıt maliyeti, döviz kuru ve sektör beklentilerinden etkilenebilir."),
            new EventData(LocalDate.now().minusDays(12), "3. Çeyrek Bilançosu", StockEvent.EventType.RISE,
                new BigDecimal("3.2"), "Beklentilerin üzerinde kar açıklandı.",
                "Şirket, 3. çeyrek karını beklentilerin üzerinde açıkladı.",
                "Bilanço açıklamaları hisse fiyatlarını kısa vadede önemli ölçüde etkileyebilir."),
            new EventData(LocalDate.now().minusDays(5), "Yakıt maliyeti endişesi", StockEvent.EventType.FALL,
                new BigDecimal("-2.1"), "Yüksek yakıt maliyetleri kar marjlarını baskılıyor.",
                "Küresel petrol fiyatlarındaki yükseliş havacılık sektörünü olumsuz etkiledi.",
                "Havayolu şirketleri yakıt maliyetlerine karşı kırılgan olabilir. Bu nedenle petrol fiyatları takip edilmesi gereken bir göstergedir.")
        ));
    }

    private void seedASELS() {
        Stock stock = new Stock();
        stock.setSymbol("ASELS");
        stock.setCompanyName("Aselsan Elektronik San. A.Ş.");
        stock.setSector("Savunma & Teknoloji");
        stock.setDescription("Türkiye'nin önde gelen savunma elektroniği şirketi. Askeri haberleşme sistemleri, radar ve elektronik harp sistemleri üretmektedir.");
        stock.setCurrentPrice(new BigDecimal("68.50"));
        stock.setDailyChangePercent(new BigDecimal("1.20"));
        stock.setPeRatio(new BigDecimal("12.5"));
        stock.setPbRatio(new BigDecimal("3.2"));
        stock.setDividendYield(new BigDecimal("1.80"));
        stock.setMarketCapBillions(new BigDecimal("92.30"));
        stock.setDataSource("SEED");
        stock.setFallback(true);
        stock = stockRepository.save(stock);

        seedPrices(stock, new BigDecimal("60.00"), 30, 5.0);
        seedEvents(stock, List.of(
            new EventData(LocalDate.now().minusDays(18), "Yeni savunma sözleşmesi", StockEvent.EventType.RISE,
                new BigDecimal("4.5"), "Büyük çaplı savunma sözleşmesi imzalandı.",
                "Şirket, önemli bir ihracat sözleşmesi imzaladığını duyurdu.",
                "Savunma şirketleri için sözleşme haberleri önemli bir katalizör olabilir."),
            new EventData(LocalDate.now().minusDays(10), "Ar-Ge yatırımı", StockEvent.EventType.NEUTRAL,
                new BigDecimal("0.5"), "Ar-Ge harcamaları arttı.",
                "Şirket, yeni teknoloji geliştirme için yatırımlarını artırdığını açıkladı.",
                "Ar-Ge harcamaları kısa vadede maliyeti artırabilir ancak uzun vadede büyümeye katkı sağlayabilir."),
            new EventData(LocalDate.now().minusDays(3), "Döviz kuru etkisi", StockEvent.EventType.FALL,
                new BigDecimal("-1.8"), "TL değer kaybı maliyet baskısı oluşturdu.",
                "Dolar/TL kurundaki yükseliş ithalat maliyetlerini artırdı.",
                "İthalat bağımlılığı olan şirketler döviz kuru dalgalanmalarına duyarlı olabilir.")
        ));
    }

    private void seedBIMAS() {
        Stock stock = new Stock();
        stock.setSymbol("BIMAS");
        stock.setCompanyName("BİM Birleşik Mağazalar A.Ş.");
        stock.setSector("Perakende & Gıda");
        stock.setDescription("Türkiye'nin en büyük indirim market zincirlerinden biri. Ülke genelinde binlerce mağazasıyla gıda ve temel tüketim ürünleri satışı yapmaktadır.");
        stock.setCurrentPrice(new BigDecimal("540.00"));
        stock.setDailyChangePercent(new BigDecimal("-0.90"));
        stock.setPeRatio(new BigDecimal("18.3"));
        stock.setPbRatio(new BigDecimal("7.5"));
        stock.setDividendYield(new BigDecimal("2.10"));
        stock.setMarketCapBillions(new BigDecimal("185.60"));
        stock.setDataSource("SEED");
        stock.setFallback(true);
        stock = stockRepository.save(stock);

        seedPrices(stock, new BigDecimal("495.00"), 30, 3.0);
        seedEvents(stock, List.of(
            new EventData(LocalDate.now().minusDays(22), "Mağaza açılış ivmesi", StockEvent.EventType.RISE,
                new BigDecimal("3.1"), "Yeni mağaza açılış hedefleri açıklandı.",
                "Şirket, yıl sonuna kadar 200 yeni mağaza açacağını duyurdu.",
                "Perakende sektöründe büyüme genellikle mağaza sayısı ve same-store satış artışıyla ölçülür."),
            new EventData(LocalDate.now().minusDays(14), "Enflasyon etkisi", StockEvent.EventType.RISE,
                new BigDecimal("2.0"), "Yüksek enflasyon ciro büyümesini destekledi.",
                "Yüksek enflasyon ortamında perakende cirolarının arttığı görüldü.",
                "Enflasyon dönemlerinde perakende şirketlerinin ciroları artabilir ancak reel büyüme ayrıca değerlendirilmelidir."),
            new EventData(LocalDate.now().minusDays(4), "Rekabet baskısı", StockEvent.EventType.FALL,
                new BigDecimal("-1.5"), "Sektördeki rekabet yoğunlaştı.",
                "Yeni rakiplerin piyasaya girişi sektörde fiyat rekabetini artırdı.",
                "Rekabet ortamındaki değişimler şirketlerin pazar payını ve karlılığını etkileyebilir.")
        ));
    }

    private void seedSISE() {
        Stock stock = new Stock();
        stock.setSymbol("SISE");
        stock.setCompanyName("Türkiye Şişe ve Cam Fabrikaları A.Ş.");
        stock.setSector("Cam & Sanayi");
        stock.setDescription("Türkiye'nin ve dünyanın önde gelen cam üreticilerinden biri. Düzcam, şişe camı, otomotiv camı ve kimyasallar alanlarında faaliyet göstermektedir.");
        stock.setCurrentPrice(new BigDecimal("38.50"));
        stock.setDailyChangePercent(new BigDecimal("0.52"));
        stock.setPeRatio(new BigDecimal("6.8"));
        stock.setPbRatio(new BigDecimal("1.4"));
        stock.setDividendYield(new BigDecimal("4.50"));
        stock.setMarketCapBillions(new BigDecimal("75.40"));
        stock.setDataSource("SEED");
        stock.setFallback(true);
        stock = stockRepository.save(stock);

        seedPrices(stock, new BigDecimal("35.00"), 30, 4.0);
        seedEvents(stock, List.of(
            new EventData(LocalDate.now().minusDays(25), "İhracat rekoru", StockEvent.EventType.RISE,
                new BigDecimal("4.8"), "Yıllık ihracat rekoru kırıldı.",
                "Şirket, ihracatını yüzde 20 artırarak rekor düzeye taşıdığını açıkladı.",
                "İhracat performansı, özellikle döviz kazanımı açısından şirket değerini olumlu etkileyebilir."),
            new EventData(LocalDate.now().minusDays(15), "Enerji maliyeti", StockEvent.EventType.FALL,
                new BigDecimal("-2.3"), "Yüksek enerji maliyetleri karlılığı baskıladı.",
                "Cam üretimi enerji yoğun bir süreç olduğundan artan enerji fiyatları maliyetleri artırdı.",
                "Enerji yoğun sektörlerde enerji fiyatları karlılık üzerinde belirleyici bir rol oynar."),
            new EventData(LocalDate.now().minusDays(7), "Temettü açıklaması", StockEvent.EventType.RISE,
                new BigDecimal("1.9"), "Nakit temettü dağıtılacağı açıklandı.",
                "Şirket, geçen yılın karından yüksek oranlı temettü dağıtacağını duyurdu.",
                "Temettü ödemeleri hissedarlar için önemli bir gelir kaynağı olabilir. Temettü verimi değerleme açısından izlenir.")
        ));
    }

    private void seedTUPRS() {
        Stock stock = new Stock();
        stock.setSymbol("TUPRS");
        stock.setCompanyName("Tüpraş-Türkiye Petrol Rafinerileri A.Ş.");
        stock.setSector("Enerji & Petrokimya");
        stock.setDescription("Türkiye'nin tek entegre petrol rafinerisi. Ham petrolü rafine ederek benzin, motorin ve diğer petrol ürünleri üretmektedir.");
        stock.setCurrentPrice(new BigDecimal("178.00"));
        stock.setDailyChangePercent(new BigDecimal("-1.10"));
        stock.setPeRatio(new BigDecimal("5.5"));
        stock.setPbRatio(new BigDecimal("2.1"));
        stock.setDividendYield(new BigDecimal("8.20"));
        stock.setMarketCapBillions(new BigDecimal("142.30"));
        stock.setDataSource("SEED");
        stock.setFallback(true);
        stock = stockRepository.save(stock);

        seedPrices(stock, new BigDecimal("160.00"), 30, 6.0);
        seedEvents(stock, List.of(
            new EventData(LocalDate.now().minusDays(19), "Petrol fiyatı yükselişi", StockEvent.EventType.RISE,
                new BigDecimal("5.2"), "Küresel petrol fiyatları yükseldi.",
                "Küresel petrol fiyatlarındaki yükseliş rafineri marjlarını olumlu etkiledi.",
                "Rafineri şirketleri ham petrol ve ürün fiyatları arasındaki fark olan 'rafineri marjından' gelir elde eder."),
            new EventData(LocalDate.now().minusDays(11), "Kapasite artışı", StockEvent.EventType.RISE,
                new BigDecimal("2.8"), "Rafineri kapasitesi genişletildi.",
                "Şirket, kapasite artırım projesini tamamladığını duyurdu.",
                "Üretim kapasitesinin artması şirketin gelir potansiyelini yükseltebilir."),
            new EventData(LocalDate.now().minusDays(6), "Döviz kuru baskısı", StockEvent.EventType.FALL,
                new BigDecimal("-2.5"), "Ham petrol ithalat maliyetleri yükseldi.",
                "TL'nin değer kaybetmesi ham petrol ithalat maliyetlerini artırdı.",
                "Dövize bağlı maliyet yapısı olan şirketler kur değişimlerine duyarlıdır.")
        ));
    }

    private void seedPrices(Stock stock, BigDecimal basePrice, int days, double volatilityPct) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        BigDecimal price = basePrice;
        double[] randomSeed = {0.8, 1.2, 0.5, -0.3, 1.5, -0.8, 0.3, 1.0, -0.5, 0.7,
                               1.1, -0.2, 0.9, 1.3, -0.6, 0.4, 1.8, -1.0, 0.6, 1.4,
                               -0.4, 0.2, 1.6, -0.7, 0.8, 1.0, -0.3, 0.5, 1.2, -0.9};

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            if (date.getDayOfWeek().getValue() >= 6) continue;
            if (stockPriceRepository.existsByStockAndPriceDate(stock, date)) continue;

            double change = randomSeed[i % randomSeed.length] * (volatilityPct / 100.0);
            BigDecimal closePrice = price.multiply(BigDecimal.valueOf(1 + change));
            BigDecimal openPrice = price.multiply(BigDecimal.valueOf(1 + change * 0.3));
            BigDecimal highPrice = closePrice.multiply(BigDecimal.valueOf(1.01));
            BigDecimal lowPrice = closePrice.multiply(BigDecimal.valueOf(0.99));

            StockPrice sp = new StockPrice();
            sp.setStock(stock);
            sp.setPriceDate(date);
            sp.setOpenPrice(openPrice.setScale(2, java.math.RoundingMode.HALF_UP));
            sp.setClosePrice(closePrice.setScale(2, java.math.RoundingMode.HALF_UP));
            sp.setHighPrice(highPrice.setScale(2, java.math.RoundingMode.HALF_UP));
            sp.setLowPrice(lowPrice.setScale(2, java.math.RoundingMode.HALF_UP));
            sp.setVolume((long) (1_000_000 + Math.random() * 5_000_000));
            sp.setDataSource("SEED");
            stockPriceRepository.save(sp);
            price = closePrice;
        }
    }

    private void seedEvents(Stock stock, List<EventData> events) {
        for (EventData e : events) {
            StockEvent event = new StockEvent();
            event.setStock(stock);
            event.setEventDate(e.date());
            event.setTitle(e.title());
            event.setEventType(e.type());
            event.setPriceChangePercent(e.changePercent());
            event.setShortDescription(e.shortDesc());
            event.setRelatedNews(e.news());
            event.setLearningNote(e.learningNote());
            stockEventRepository.save(event);
        }
    }

    private void seedLessons() {
        if (lessonRepository.count() > 0) return;

        List<LessonData> lessons = List.of(
            new LessonData("Borsa Nedir?", "borsa-nedir", "storefront", "primary-container",
                "Alıcıların ve satıcıların hisse senetleri gibi finansal ürünleri alıp sattığı organize bir pazar yeridir.",
                "Borsa, şirketlerin halka açılarak sermaye toplamasına ve yatırımcıların bu şirketlere ortak olmasına olanak tanır. Türkiye'de Borsa İstanbul (BIST) bu işlevi üstlenmektedir.",
                "Borsa fiyatları arz ve talebe göre her an değişebilir. Bu nedenle yatırım yapmadan önce iyi araştırma yapmak önemlidir.",
                "Bir şirketin hissesini aldığınızda o şirketin küçük bir ortağı olursunuz.",
                "Neden önemli? Tasarruflarınızı büyütmek için borsayı anlamak ilk adımdır.", 1),
            new LessonData("Hisse Nedir?", "hisse-nedir", "pie_chart", "tertiary-container",
                "Bir şirketin sahipliğinin küçük bir parçasıdır. Hisse aldığınızda, o şirkete ortak olursunuz.",
                "Şirketler büyüme sermayesi toplamak için halka arz (IPO) yaparak hisselerini borsada satışa sunar. Her hisse, şirketteki eşit bir sahiplik payını temsil eder.",
                "Hisse fiyatı şirketin değeri, beklentiler ve piyasa koşullarına göre sürekli değişir. Fiyatın yükselmesi tek başına bir gösterge değildir.",
                "100 TL'ye THYAO hissesi aldığınızda Türk Hava Yolları'nın küçük bir ortağı olursunuz.",
                "Neden önemli? Hissenin ne olduğunu anlamadan borsa yatırımı yapmak risklidir.", 2),
            new LessonData("F/K Oranı Nedir?", "fk-orani-nedir", "calculate", "secondary-container",
                "Fiyat/Kazanç oranı. Bir şirketin hisse fiyatının, hisse başına düşen karına oranıdır.",
                "F/K oranı, yatırımcıların şirketin 1 birim kazancı için kaç birim ödemeye razı olduğunu gösterir. Düşük F/K ucuz, yüksek F/K pahalı anlamına gelebilir — ama her zaman değil.",
                "F/K oranı tek başına bir yatırım kararı için yeterli değildir. Sektör ortalaması ve şirketin büyüme beklentileriyle birlikte değerlendirilmelidir.",
                "F/K=10 ise şirketin mevcut karını 10 yılda çıkaracağı anlamına gelir.",
                "Neden önemli? Bir hissenin pahalı mı ucuz mu olduğunu anlamak için ilk bakılacak göstergelerden biridir.", 3),
            new LessonData("PD/DD Nedir?", "pd-dd-nedir", "balance", "primary-container",
                "Piyasa Değeri / Defter Değeri oranı. Şirketin piyasada işlem gördüğü değerin, muhasebe kayıtlarındaki net varlık değerine oranıdır.",
                "PD/DD < 1 ise şirket defter değerinin altında işlem görüyor demektir. Bu ucuzluk sinyali olabilir ama düşük olmasının başka nedenleri de olabilir.",
                "Bankacılık ve finans sektöründe daha anlamlıdır. Teknoloji şirketleri için çok yüksek olabilir.",
                "Bir şirketin varlıkları 100 TL, piyasa değeri 80 TL ise PD/DD = 0.8'dir.",
                "Neden önemli? Şirketin piyasanın ona biçtiği değeri anlamak için kullanılır.", 4),
            new LessonData("Temettü Nedir?", "temettü-nedir", "payments", "primary-container",
                "Şirketin elde ettiği karın bir kısmını hisse sahiplerine nakit olarak dağıtmasıdır.",
                "Şirketler her yıl kar elde ettiklerinde bu karı tekrar işe yatırabilir veya hissedarlara temettü olarak dağıtabilir. Temettü alan yatırımcılar düzenli gelir elde eder.",
                "Yüksek temettü veren şirketler her zaman iyi yatırım olmayabilir. Temettünün sürdürülebilir olup olmadığına bakılmalıdır.",
                "1000 hisseniz varsa ve hisse başına 5 TL temettü dağıtılıyorsa 5000 TL nakit alırsınız.",
                "Neden önemli? Temettü, pasif gelir arayan yatırımcılar için önemli bir kriterdir.", 5),
            new LessonData("Volatilite Nedir?", "volatilite-nedir", "trending_up", "primary-container",
                "Bir hisse senedinin fiyatının ne kadar hızlı ve ne kadar büyük değişimler gösterdiğinin ölçüsüdür.",
                "Yüksek volatilite, fiyatın kısa sürede büyük yüzdelik değişimler yaşadığı anlamına gelir. Düşük volatilite ise fiyatın görece istikrarlı hareket ettiğini gösterir.",
                "Yüksek volatilite hem yüksek kazanç hem de yüksek kayıp anlamına gelebilir. Yeni başlayanlar için düşük volatiliteli hisseler daha az risklidir.",
                "Bir hisse bir gün %5 artıp ertesi gün %4 düşüyorsa yüksek volatiliteye sahiptir.",
                "Neden önemli? Risk toleransınıza uygun hisseler seçmek için volatiliteyi anlamanız gerekir.", 6),
            new LessonData("Risk Nedir?", "risk-nedir", "warning", "tertiary-container",
                "Yatırımınızın beklediğinizden farklı performans göstermesi ya da değer kaybetmesi ihtimalidir.",
                "Her yatırım belirli bir risk taşır. Hisse senetleri, tahviller ve mevduatın risk seviyeleri farklıdır. Çeşitlendirme riski dağıtmanın temel yöntemidir.",
                "Yüksek getiri potansiyeli genellikle yüksek riskle birlikte gelir. Kaybedemeyeceğiniz parayı yatırıma sokmayın.",
                "Tüm paranızı tek bir hisseye yatırmak yerine farklı sektörlerden hisseler almak riski azaltır.",
                "Neden önemli? Riski anlamadan yatırım yapmak büyük kayıplara yol açabilir.", 7),
            new LessonData("Grafik Nasıl Okunur?", "grafik-nasil-okunur", "show_chart", "primary-container",
                "Hisse fiyat grafikleri zamanla fiyatın nasıl değiştiğini görsel olarak gösterir.",
                "Çizgi grafikler kapanış fiyatlarını birbirine bağlar. Yatay eksen zamanı, dikey eksen fiyatı gösterir. Yükselen bir çizgi fiyatın arttığını, alçalan bir çizgi düştüğünü gösterir.",
                "Grafikler geçmiş veriyi gösterir. Geçmiş performans gelecekteki performansı garantilemez.",
                "Bir çizgi grafikte sol taraftaki fiyat ile sağ taraftaki fiyatı karşılaştırarak yüzde değişimi hesaplayabilirsiniz.",
                "Neden önemli? Fiyat hareketini anlamak için grafik okumak temel bir beceridir.", 8),
            new LessonData("Haberler Fiyatı Nasıl Etkiler?", "haberler-fiyat-etkisi", "newspaper", "secondary-container",
                "Şirket, sektör veya ekonomiye dair haberler yatırımcıların kararlarını etkileyerek hisse fiyatlarını değiştirebilir.",
                "Olumlu haberler (güçlü bilanço, yeni ürün, büyüme planı) fiyatı artırabilir. Olumsuz haberler (zarar, dava, sektör krizi) fiyatı düşürebilir. Ancak piyasa bazen beklenmedik tepkiler verebilir.",
                "Bir haberin fiyata etkisi tahmin edilemeyebilir. Piyasa zaten beklenen haberleri önceden fiyatlamış olabilir.",
                "Bir havayolunun yolcu rekoru kırdığını açıklaması, havacılık hisselerinin yükselmesine neden olabilir.",
                "Neden önemli? Haberleri takip etmek yatırım kararlarınızı daha bilinçli yapmanıza yardımcı olur.", 9),
            new LessonData("Bilanço Nedir?", "bilanco-nedir", "description", "primary-container",
                "Bir şirketin belirli bir andaki varlıklarını, borçlarını ve özkaynaklarını gösteren finansal tablodur.",
                "Bilanço 3 temel bileşenden oluşur: Varlıklar (şirketin sahip oldukları), Borçlar (ödenmesi gerekenler) ve Özkaynaklar (sahipler kısmı). Varlıklar = Borçlar + Özkaynaklar formülü her zaman geçerlidir.",
                "Bilançoyu doğru yorumlamak için diğer dönemlerle ve sektör ortalamasıyla karşılaştırmak gerekir.",
                "Bir şirketin 100 TL varlığı ve 60 TL borcu varsa özkaynaği 40 TL'dir.",
                "Neden önemli? Bir şirketin finansal sağlığını anlamak için bilançoyu okumak kritiktir.", 10)
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

    private record EventData(LocalDate date, String title, StockEvent.EventType type,
                             BigDecimal changePercent, String shortDesc, String news, String learningNote) {}

    private record LessonData(String title, String slug, String icon, String accentColor,
                              String shortDesc, String content, String warning, String example,
                              String whyItMatters, int sortOrder) {}
}
