# BasitBorsa — Mimari Dokümantasyonu

## Genel Bakış

BasitBorsa üç katmanlı bir monorepo yapısına sahiptir:

```
Browser (React SPA)
       │
       │ HTTP /api/*
       ▼
Spring Boot REST API (port 8080)
       │
       ├── PostgreSQL (kalıcı veri)
       └── Gemini API (AI açıklamaları)
```

## Frontend

- **React 18** + **Vite** — hızlı geliştirme ve build
- **Tailwind CSS** — tasarım sistemi tokenları HTML sınıflarına map edilmiş
- **React Router v6** — client-side routing
- **Recharts** — hisse fiyat grafikleri
- **Axios** — API çağrıları; `/api` prefix'i Vite proxy'si üzerinden backend'e yönlendirilir

### Dizin yapısı (frontend/src)

```
app/          — Router ve App kök bileşeni
pages/        — Sayfa bileşenleri (Home, Stocks, StockDetail, Learn, Portfolio)
components/
  layout/     — AppShell, TopNav, BottomNav
  common/     — Card, Button, LoadingState, ErrorState, Disclaimer
  stock/      — StockCard, StockChart, StoryPanel, MetricCard
  ai/         — AiTeacherBox
  portfolio/  — PortfolioCard, ChecklistModal
  learning/   — LessonCard, LearningNote
services/     — api.js + stockService, portfolioService, aiService, lessonService
hooks/        — useAsync (genel veri fetching hook)
utils/        — formatters.js (para/tarih/yüzde formatlama)
styles/       — index.css (Tailwind + custom component sınıfları)
```

## Backend

### Katman yapısı

```
Controller → Service → Repository → Entity (JPA)
                     → Provider (MarketDataProvider)
                     → External APIs (Gemini)
```

| Katman | Sorumluluk |
|--------|-----------|
| Controller | HTTP request/response, routing, validation çağrısı |
| Service | Business logic, orchestration |
| Repository | Veritabanı erişimi (Spring Data JPA) |
| Entity | JPA domain modeli |
| DTO | API request/response nesneleri |
| Mapper | Entity ↔ DTO dönüşümü |
| Provider | Piyasa verisi sağlayıcı arayüzü |
| Seed | Başlangıç verisi (CommandLineRunner) |

### Piyasa Verisi Akışı

```
1. Uygulama başlar → DatabaseSeeder seed verisi yükler
2. GET /api/stocks/{symbol}/prices → StockService
3. StockService → StockPriceRepository (PostgreSQL'den)
4. (Gelecekte) @Scheduled sync → MarketDataProvider → PostgreSQL cache
5. Provider başarısız → en son cached veri veya seed fallback
```

### AI Akışı

```
POST /api/ai/explain-event
  → AiService.explainEvent()
  → Cache key kontrolü (AiExplanationRepository)
  → Cache hit → cached yanıt döner
  → Cache miss → Gemini API çağrısı
  → Yanıt PostgreSQL'e kaydedilir
  → Gemini başarısız → güvenli fallback döner
```

## Veritabanı

PostgreSQL. JPA/Hibernate DDL auto=update (geliştirme için). Production'da Flyway/Liquibase önerilir.

## Güvenlik

- API key'ler yalnızca backend'de, env vars üzerinden
- CORS yalnızca `FRONTEND_ORIGIN` için açık
- Global exception handler — stack trace hiçbir zaman frontend'e dönmez
- Input validation: Jakarta Bean Validation (@NotBlank, @Min, vb.)
