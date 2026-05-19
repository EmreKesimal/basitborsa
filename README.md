# BasitBorsa  Deploy Link: https://basitborsa-frontend.onrender.com 

> **Borsayı anlamanın en basit yolu**

BasitBorsa, yeni başlayanların borsayı sade ve anlaşılır şekilde öğrenmesi için
geliştirilmiş, yapay zekâ destekli bir **demo eğitim platformudur**.

Kullanıcılar seçili BIST hisselerini inceleyebilir, "Grafiğin Hikâyesi" paneli ile
fiyat hareketlerinin olası nedenlerini öğrenebilir, ilgili haberleri kategoriler
halinde okuyabilir ve AI Öğretmen üzerinden temel finansal kavramları sorabilir.

> Bu platform yatırım tavsiyesi vermez.
> Hisse alım/satım önerisi içermez ve sanal portföy/simülasyon özelliği MVP'de yer almaz.
> Gösterilen bilgiler eğitim ve demo amaçlıdır; veriler gecikmeli/gün sonu olabilir.

---

## Mimari

```
Frontend (React) ──HTTP──> Spring Boot Backend ──HTTP──> Python AI Service
                                       │                  (Gemini)
                                       └──> PostgreSQL
                                       └──> Twelve Data (market data)
                                       └──> RSS / haber kaynakları
```

- Frontend **yalnızca** Spring Boot backend'ine istek atar.
- Piyasa verisi, haber toplama, cache ve AI bağlam hazırlığı backend'in sorumluluğundadır.
- Python AI service yalnızca Gemini istemcisidir; piyasa verisi veya haber **çekmez**,
  tüm bağlamı backend'den hazır alır.
- Tüm API anahtarları yalnızca ilgili servisin ortam değişkenlerinde tutulur.
  Frontend hiçbir koşulda Gemini ya da Twelve Data anahtarına erişmez.

---

## Teknoloji Yığını

| Katman      | Teknoloji                                            |
|-------------|------------------------------------------------------|
| Frontend    | React 18, Vite, Tailwind CSS, React Router, Recharts |
| Backend     | Java 21, Spring Boot 3.2, Spring Data JPA            |
| AI Service  | Python 3.11+, FastAPI, Google Generative AI SDK      |
| Veritabanı  | PostgreSQL 16                                        |
| AI sağlayıcı| Google Gemini (sadece AI service erişir)             |
| Piyasa veri | Twelve Data (sadece backend erişir)                  |
| Infra       | Docker Compose                                       |

---

## Proje Yapısı

```
basitborsa/
├── backend/         # Spring Boot uygulaması
├── frontend/        # React + Vite uygulaması
├── ai-service/      # Python FastAPI Gemini servisi
├── infra/           # Ek Dockerfile'lar
├── docs/            # Mimari ve API dokümantasyonu
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## Hızlı Başlangıç

### 1. Ortam değişkenlerini ayarla

```bash
cp .env.example .env
# .env içindeki API anahtarlarını gerçek değerlerle doldur
```

Gerekli en az değişkenler:

- `POSTGRES_*` — yerel veya container PostgreSQL bilgileri
- `GEMINI_API_KEY` — Google AI Studio anahtarı (yalnız AI service okur)
- `MARKET_DATA_API_KEY` / `TWELVE_DATA_API_KEY` — Twelve Data anahtarı (yalnız backend okur)
- `INTERNAL_AI_API_KEY` — backend ↔ AI service arası dahili anahtar
- `AI_SERVICE_BASE_URL` — backend'in AI servise eriştiği URL

### 2. PostgreSQL

```bash
docker run -d --name basitborsa-pg \
  -e POSTGRES_DB=basitborsa \
  -e POSTGRES_USER=basitborsa \
  -e POSTGRES_PASSWORD=basitborsa_secret \
  -p 5432:5432 postgres:16-alpine
```

### 3. Backend (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run
```

Backend `http://localhost:8080` adresinde başlar.

### 4. Python AI Service

```bash
cd ai-service
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python -m uvicorn app.main:app --reload --reload-dir app --port 8000
```

AI service `http://localhost:8000` adresinde başlar.

Sağlık kontrolü:

```bash
curl http://localhost:8000/health
```

### 5. Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend `http://localhost:5173` adresinde başlar.

---

## Ortam Değişkenleri

| Değişken | Kim okur | Açıklama |
|----------|----------|----------|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_HOST`, `POSTGRES_PORT` | Backend | PostgreSQL bağlantısı |
| `FRONTEND_ORIGIN` | Backend | CORS izinli origin |
| `MARKET_DATA_PROVIDER` | Backend | `twelve-data` veya `fallback` |
| `MARKET_DATA_API_KEY` (veya `TWELVE_DATA_API_KEY`) | Backend | Twelve Data anahtarı |
| `MARKET_DATA_BASE_URL` | Backend | `https://api.twelvedata.com` |
| `MARKET_DATA_EXCHANGE` | Backend | `BIST` |
| `AI_SERVICE_BASE_URL` | Backend | Python AI service URL'i |
| `INTERNAL_AI_API_KEY` | Backend + AI service | Backend ↔ AI service arası gizli anahtar |
| `GEMINI_API_KEY` | **Yalnız** AI service | Google Gemini anahtarı |
| `GEMINI_MODEL` | AI service | Önerilen: `gemini-2.5-flash-lite` |
| `NEWS_*`, `KAP_*` | Backend | Haber toplama ayarları (opsiyonel) |

> Bu repodaki **`.env.example` içinde gerçek anahtar yer almaz**. Gerçek
> anahtarlar yalnızca yerelde `.env` dosyasında veya production secret store
> içinde tutulmalı, asla commit'lenmemelidir. `.env` dosyası `.gitignore` altındadır.

---

## API Endpoint Özeti

Hisse verileri:

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/api/stocks` | Desteklenen hisseler |
| GET | `/api/stocks/{symbol}` | Hisse detayı |
| GET | `/api/stocks/{symbol}/prices?range=30d` | Fiyat geçmişi |
| GET | `/api/stocks/{symbol}/events` | Önemli olaylar |
| GET | `/api/stocks/{symbol}/news?limit=10` | Bağlamsal haberler |

AI:

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| POST | `/api/ai/chart-story` | Grafik hikâyesi (Gemini, eğitim odaklı) |
| POST | `/api/ai/explain-event` | Olay açıklaması |
| POST | `/api/ai/explain-term` | Terim açıklaması |
| POST | `/api/ai/explain-stock` | Hisse açıklaması |

Eğitim içerikleri:

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/api/lessons` | Dersleri listele |
| GET | `/api/lessons/{slug}` | Ders detayı |

Yönetim (admin):

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| POST | `/api/admin/market-data/sync-selected` | Manuel veri senkronizasyonu |
| GET | `/api/admin/market-data/sync-status` | Senkronizasyon durumu |

> Admin endpoint'leri production'da network seviyesinde kısıtlanmalı veya
> kimliklendirme arkasına alınmalıdır.

---

## Demo Akışı

1. `/` — Hero "**Borsayı anlamanın en basit yolu**" sloganı, üç adımlık nasıl-çalışır
   bölümü ve desteklenen hisseler.
2. `/stocks` — Desteklenen BIST hisselerinin listesi (gecikmeli/gün sonu).
3. `/stocks/THYAO` — Ana demo sayfası: gerçek/cached fiyat geçmişi, etkileşimli
   "Grafiğin Hikâyesi" paneli, kategori etiketli haberler ve AI Öğretmen.
4. `/learn` — F/K, PD/DD, temettü gibi temel kavramların eğitim kartları.

> THYAO bilinçli olarak gerçek veri akışına en yakın demo hissedir. Diğer demo
> hisselerinin görsel olarak benzer olması, tüm hisselerin tam gerçek veriyle
> beslendiği anlamına gelmez.

---

## Güvenlik ve Uyarılar

- `GEMINI_API_KEY` yalnızca Python AI service'in ortam değişkenlerinden okunur.
- `MARKET_DATA_API_KEY` / `TWELVE_DATA_API_KEY` yalnızca backend'den okunur.
- Frontend hiçbir koşulda Gemini veya Twelve Data anahtarına erişmez.
- AI yanıtları **eğitim amaçlıdır**, yatırım tavsiyesi içermez ve "kesin yükselir",
  "alım fırsatı", "alınmalı", "satılmalı" gibi yönlendirici ifadeleri filtreler.
- MVP'de **sanal portföy / simülasyon yoktur**. Alım-satım butonları, sanal bakiye,
  pozisyon takibi vb. özellikler yer almaz.
- AI servisinin fallback mekanizması: Gemini hata verdiğinde güvenli, sabit
  açıklayıcı metinler döner; uygulama çökmez.

---

## Bilinen Kısıtlamalar

- Tek demo kullanıcı; gerçek auth sistemi yok.
- Piyasa verisi gecikmeli/gün sonudur, gerçek-zamanlı tick verisi sunulmaz.
- Tüm hisseler için tam gerçek-veri desteği yoktur; THYAO en kapsamlı demo'dur.
- AI yanıtları Gemini erişimi gerektirir; anahtar yoksa veya quota dolduğunda
  güvenli fallback metinleri görüntülenir.

---

## Sonraki Adımlar

- Daha fazla BIST hissesinde tam gerçek-veri kapsamı.
- Quiz tabanlı kişisel öğrenme yolu.
- Admin paneli (seed/news/events yönetimi) ve erişim kontrolü.
- İstemci tarafı i18n.
