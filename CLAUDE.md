# BasitBorsa — Claude Project Context

## Project
BasitBorsa is an AI-assisted stock market learning platform for beginners.

Not a trading platform.
No investment advice.
No buy/sell recommendations.
No virtual portfolio in MVP.

Core slogan:
Hisseyi alma, önce anla.

## Stack
- Frontend: React + Vite + Tailwind
- Backend: Spring Boot + PostgreSQL
- AI Service: Python FastAPI
- AI Provider: Gemini
- Market Data: Twelve Data through Spring Boot only

## Important Architecture
Frontend only calls Spring Boot.
Spring Boot owns market data, cache, fallback, AI context.
Python AI service only handles Gemini prompts, safety, structured educational responses.
Python must not fetch market data.
Gemini API key only lives in ai-service.
Twelve Data key only lives in backend.

## Existing API Contract
Do not break:

GET /api/stocks
GET /api/stocks/{symbol}
GET /api/stocks/{symbol}/prices?range=30d
GET /api/stocks/{symbol}/events

AI endpoints:

POST /api/ai/chart-story
POST /api/ai/explain-event
POST /api/ai/explain-term
POST /api/ai/explain-stock

## Protected Files
Never delete, rename, move, overwrite, or refactor files/folders containing:

- ruflo
- Ruflo
- RUFLO
- ruFLO

Before big changes run:

find . -iname "*ruflo*" -print

Final diff must not include ruflo files.

## Current AI Service Status
Python AI service runs on:

http://localhost:8000

Start command:

cd ai-service
source .venv/bin/activate
python -m uvicorn app.main:app --reload --reload-dir app --port 8000

Health test:

curl http://localhost:8000/health

Internal API key:

INTERNAL_AI_API_KEY=local-dev-secret

## Gemini Status
Important discovery:

gemini-2.0-flash returns 429 quota error.
gemini-1.5-flash returns 404 on current API.
gemini-2.5-flash-lite works.

Use:

GEMINI_MODEL=gemini-2.5-flash-lite

Working direct Gemini test:

curl "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent" \
-H "x-goog-api-key: $GEMINI_API_KEY" \
-H "Content-Type: application/json" \
-X POST \
-d '{
"contents": [
{
"parts": [
{
"text": "Sadece şu kelimeyi yaz: çalışıyor"
}
]
}
]
}'

Expected text:
çalışıyor

## AI Chart Story Status
Direct Python chart-story works and returns:

sourceType=ai_generated

Test:

curl -X POST http://localhost:8000/ai/chart-story \
-H "Content-Type: application/json" \
-H "X-Internal-Api-Key: local-dev-secret" \
-d '{
"symbol":"THYAO",
"companyName":"Türk Hava Yolları",
"sector":"Havacılık",
"priceContext":{
"range":"30d",
"latestClose":305.4,
"previousClose":298.2,
"changePercent":2.41,
"highestPrice":312.8,
"lowestPrice":284.1,
"volumeTrend":"above_average",
"dataSource":"CACHED"
},
"selectedEvent":{
"eventDate":"2025-03-11",
"eventTitle":"Güçlü yükseliş",
"priceChangePercent":5.9,
"relatedNews":["Yolcu sayılarında artış açıklandı"],
"learningNote":"Havacılık hisseleri yolcu sayısı, yakıt maliyeti, döviz kuru ve sektör beklentilerinden etkilenebilir."
},
"userLevel":"beginner",
"language":"tr"
}'

## Current Next Step
Test and finalize Spring Boot -> Python AI integration.

Run:

curl -X POST http://localhost:8080/api/ai/chart-story \
-H "Content-Type: application/json" \
-d '{"symbol":"THYAO"}'

Expected:
sourceType=ai_generated

If Spring returns fallback but Python direct test works:
- check AI_SERVICE_BASE_URL
- check INTERNAL_AI_API_KEY
- check Spring payload shape
- check Spring fallback/cache logic

## Required Env

backend:

TWELVE_DATA_API_KEY=
TWELVE_DATA_BASE_URL=
MARKET_DATA_PROVIDER=TWELVE_DATA
AI_SERVICE_BASE_URL=http://localhost:8000
INTERNAL_AI_API_KEY=local-dev-secret

ai-service:

GEMINI_API_KEY=
GEMINI_MODEL=gemini-2.5-flash-lite
INTERNAL_AI_API_KEY=local-dev-secret

## Language Rules
AI answers must be:
- Turkish
- beginner-friendly
- cautious
- educational
- no investment advice
- no buy/sell recommendation
- no certain prediction

Prefer:
- katkı sağlamış olabilir
- ilişkili olabilir
- yatırımcı algısını etkilemiş olabilir
- tek ve kesin nedeni olmayabilir

Avoid:
- bu yüzden yükseldi
- kesin yükselir
- alınmalı
- satılmalı
- kazandırır
- alım fırsatı