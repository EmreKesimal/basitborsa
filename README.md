# BasitBorsa

> **Hisseyi alma, önce anla.**

BasitBorsa, yeni başlayanların borsayı sade ve anlaşılır şekilde öğrenmesi için geliştirilmiş yapay zekâ destekli bir demo eğitim platformudur.

Kullanıcılar seçili BIST hisselerini inceleyebilir, grafik hareketlerinin hikâyesini ("Grafiğin Hikâyesi") görebilir, temel finansal kavramları öğrenebilir ve 100.000 ₺ sanal bakiyeyle risksiz pratik yapabilir.

> Bu proje yatırım tavsiyesi vermez. Gerçek alım-satım işlemi veya gerçek para kullanımı içermez.
> Veriler gecikmeli/gün sonu olabilir.

---

## Teknoloji Yığını

| Katman      | Teknoloji                                         |
|-------------|---------------------------------------------------|
| Frontend    | React 18, Vite, Tailwind CSS, React Router, Recharts |
| Backend     | Java 21, Spring Boot 3.2, Spring Data JPA         |
| Veritabanı  | PostgreSQL 16                                     |
| AI          | Google Gemini API (backend only)                  |
| Infra       | Docker Compose                                    |

---

## Proje Yapısı

```
basitborsa/
├── backend/          # Spring Boot uygulaması
├── frontend/         # React + Vite uygulaması
├── infra/            # Ek Dockerfile’lar
├── docs/             # Mimari ve API dokümantasyonu
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## Hızlı Başlangıç (Docker Compose)

### 1. Ortam değişkenlerini ayarla

```bash
cp .env.example .env
# .env dosyasını düzenle (GEMINI_API_KEY, vb.)
```

### 2. Docker ile çalıştır

```bash
docker compose up --build
```

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- PostgreSQL: localhost:5432

Seed verisi otomatik olarak yüklenir. Gemini API anahtarı olmasa bile uygulama fallback mesajlarıyla çalışır.

---

## Lokal Geliştirme

### Gereksinimler

- Java 21+
- Maven 3.9+
- Node.js 20+
- PostgreSQL 16 (veya Docker)

### PostgreSQL kurulumu (Docker ile)

```bash
docker run -d \
  --name basitborsa-pg \
  -e POSTGRES_DB=basitborsa \
  -e POSTGRES_USER=basitborsa \
  -e POSTGRES_PASSWORD=basitborsa_secret \
  -p 5432:5432 \
  postgres:16-alpine
```

### Backend

```bash
cd backend
./mvnw spring-boot:run
# veya
mvn spring-boot:run
```

Backend `http://localhost:8080` adresinde başlar.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend `http://localhost:5173` adresinde başlar. API istekleri `/api` proxy’si üzerinden backend’e yönlendirilir.

---

## Ortam Değişkenleri

| Değişken | Açıklama | Varsayılan |
|----------|----------|-----------|
| `POSTGRES_DB` | Veritabanı adı | `basitborsa` |
| `POSTGRES_USER` | Kullanıcı adı | `basitborsa` |
| `POSTGRES_PASSWORD` | Şifre | — |
| `POSTGRES_HOST` | Host | `localhost` |
| `POSTGRES_PORT` | Port | `5432` |
| `GEMINI_API_KEY` | Google Gemini API anahtarı | boş (fallback modu) |
| `MARKET_DATA_PROVIDER` | Piyasa verisi sağlayıcısı (`twelve-data` veya `fallback`) | `fallback` |
| `MARKET_DATA_API_KEY` | Twelve Data API anahtarı | boş (seed veri) |
| `MARKET_DATA_BASE_URL` | Twelve Data API base URL | `https://api.twelvedata.com` |
| `MARKET_DATA_EXCHANGE` | Borsa kodu (`BIST` çalışıyor, `XIST` çalışmıyor) | `BIST` |
| `FRONTEND_ORIGIN` | CORS için frontend URL | `http://localhost:5173` |

---

## Seed / Fallback Veri

Uygulama ilk başlatıldığında `DatabaseSeeder` otomatik olarak şu verileri yükler:

- **5 hisse:** THYAO, ASELS, BIMAS, SISE, TUPRS
- Her hisse için ~30 günlük fiyat geçmişi
- Her hisse için 3 önemli olay (StockEvent)
- **10 eğitim dersi** (Öğren sayfası)
- **Demo kullanıcı** ve başlangıç portföyü (100.000 ₺)

Seed verisi sadece veritabanı boşsa yüklenir; mevcut verinin üzerine yazmaz.

---

## API Endpoint Özeti

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/api/stocks` | Tüm hisseleri listele |
| GET | `/api/stocks/{symbol}` | Hisse detayı |
| GET | `/api/stocks/{symbol}/events` | Hisse olayları |
| GET | `/api/stocks/{symbol}/prices?range=30d` | Fiyat geçmişi |
| POST | `/api/ai/explain-event` | Olay açıklaması (Gemini) |
| POST | `/api/ai/explain-term` | Terim açıklaması (Gemini) |
| POST | `/api/ai/explain-stock` | Hisse açıklaması (Gemini) |
| GET | `/api/portfolio` | Demo portföyü getir |
| POST | `/api/portfolio/items` | Portföye hisse ekle |
| DELETE | `/api/portfolio/items/{id}` | Portföyden hisse çıkar |
| GET | `/api/lessons` | Tüm dersleri listele |
| GET | `/api/lessons/{slug}` | Ders detayı |
| POST | `/api/admin/market-data/sync-selected` | Manuel veri senkronizasyonu |
| GET | `/api/admin/market-data/sync-status` | Senkronizasyon durumu |

---

## Gemini AI Kurulumu

1. [Google AI Studio](https://aistudio.google.com)’dan API anahtarı alın.
2. `.env` dosyasına ekleyin: `GEMINI_API_KEY=your_key_here`
3. Yeniden başlatın.

API anahtarı yoksa AI endpoint’leri güvenli fallback mesajları döner — uygulama çökmez.

---

## Piyasa Verisi Sağlayıcısı

Mimari bir `MarketDataProvider` arayüzü üzerine kurulmuştur:
- `FallbackMarketDataProvider`: Seed verisi kullanır (varsayılan, env ayarlanmadıysa)
- `ExternalMarketDataProvider`: Twelve Data API üzerinden gecikmeli/gün sonu BIST verisi çeker

### Twelve Data Kurulumu

1. [Twelve Data](https://twelvedata.com) üzerinden API anahtarı alın (ücretsiz plan desteklenir).
2. `.env` dosyasına ekleyin:
   ```
   MARKET_DATA_PROVIDER=twelve-data
   MARKET_DATA_API_KEY=your_twelve_data_api_key
   MARKET_DATA_BASE_URL=https://api.twelvedata.com
   MARKET_DATA_EXCHANGE=BIST
   ```
3. Yeniden başlatın.

API anahtarı yoksa veya `MARKET_DATA_PROVIDER=fallback` ise uygulama seed verisiyle çalışmaya devam eder.

Desteklenen semboller: THYAO, ASELS, BIMAS, SISE, TUPRS, KCHOL, GARAN, FROTO

Veri senkronizasyonu manuel olarak tetiklenebilir:
```bash
curl -X POST http://localhost:8080/api/admin/market-data/sync-selected
```
Otomatik senkronizasyon hafta içi her gün 19:00 (Europe/Istanbul) çalışır.

---

## Güvenlik ve Uyarılar

- Gemini API anahtarı yalnızca backend’de kullanılır; frontend’e hiçbir zaman iletilmez.
- Piyasa verisi API anahtarı da sadece backend’dedir.
- AI yanıtları eğitim amaçlıdır; yatırım tavsiyesi içermez.
- Tüm portföy işlemleri sabittir ve gerçek para içermez.

---

## Bilinen Kısıtlamalar

- Tek demo kullanıcı; gerçek auth sistemi yok.
- Piyasa verisi sağlayıcısı stub’dır; gerçek veri için entegrasyon gerekir.
- AI açıklamaları Gemini API gerektirdiğinden anahtarsız kullanımda fallback döner.

## Sonraki Adımlar

- Gerçek piyasa verisi entegrasyonu (Alpha Vantage, vb.)
- Kullanıcı authentication sistemi
- Daha fazla hisse ve ders
- Quiz sistemi ve kişiselleştirilmiş öğrenme yolu
- Admin paneli (seed/news/events yönetimi)
