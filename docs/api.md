# BasitBorsa — API Dokümantasyonu

Base URL: `http://localhost:8080/api`

---

## Hisseler

### GET /stocks
Tüm desteklenen hisseleri listeler.

**Response:**
```json
[
  {
    "id": 1,
    "symbol": "THYAO",
    "companyName": "Türk Hava Yolları A.O.",
    "sector": "Havacılık & Ulaşım",
    "description": "...",
    "currentPrice": 305.25,
    "dailyChangePercent": 2.45,
    "peRatio": 4.2,
    "pbRatio": 1.1,
    "dividendYield": 0.00,
    "marketCapBillions": 420.50,
    "dataSource": "SEED",
    "isFallback": true,
    "disclaimer": "Veriler gecikmeli/gün sonu olabilir..."
  }
]
```

### GET /stocks/{symbol}
Tek hisse detayı döner.

### GET /stocks/{symbol}/events
Hisseye ait önemli olayları döner.

**Response:**
```json
[
  {
    "id": 1,
    "eventDate": "2026-04-22",
    "title": "Güçlü yolcu artışı",
    "eventType": "RISE",
    "priceChangePercent": 5.9,
    "shortDescription": "...",
    "relatedNews": "...",
    "learningNote": "..."
  }
]
```

### GET /stocks/{symbol}/prices?range=30d
Fiyat geçmişi. `range` parametresi: `7d`, `30d`, `90d`, `180d`, `1y`

**Response:**
```json
{
  "symbol": "THYAO",
  "dataSource": "SEED",
  "isFallback": true,
  "lastUpdatedAt": "2026-05-12T10:00:00",
  "prices": [
    {
      "date": "2026-04-12",
      "open": 278.50,
      "high": 282.00,
      "low": 276.00,
      "close": 280.25,
      "volume": 3200000
    }
  ],
  "disclaimer": "Veriler gecikmeli/gün sonu olabilir..."
}
```

---

## AI Açıklamaları

### POST /ai/explain-event
Grafik olayı için AI açıklaması.

**Request:**
```json
{
  "symbol": "THYAO",
  "companyName": "Türk Hava Yolları",
  "eventDate": "2026-04-22",
  "priceChangePercent": 5.9,
  "eventTitle": "Güçlü yükseliş",
  "relatedNews": ["Yolcu sayısı arttı", "Sektörde olumlu beklentiler"]
}
```

**Response:**
```json
{
  "summary": "Bu dönemde hissede güçlü bir yükseliş görülüyor.",
  "possibleFactors": [
    "Yolcu sayılarındaki artış yatırımcı algısını olumlu etkilemiş olabilir."
  ],
  "learningNote": "Hisseler sadece şirket haberleriyle değil, sektör gelişmeleriyle de hareket edebilir.",
  "disclaimer": "Bu açıklama yatırım tavsiyesi değildir.",
  "cached": false
}
```

### POST /ai/explain-term
Finansal terim açıklaması.

**Request:** `{ "term": "F/K oranı" }`

### POST /ai/explain-stock
Hisse veya genel soru açıklaması.

**Request:**
```json
{
  "symbol": "THYAO",
  "companyName": "Türk Hava Yolları",
  "sector": "Havacılık",
  "question": "Bu şirketi basitçe anlat"
}
```

---

## Portföy

### GET /portfolio
Demo kullanıcının sanal portföyünü döner.

**Response:**
```json
{
  "id": 1,
  "virtualBalance": 93500.00,
  "totalStockValue": 6500.00,
  "totalValue": 100000.00,
  "totalGainLoss": 500.00,
  "totalGainLossPercent": 8.33,
  "items": [
    {
      "id": 1,
      "symbol": "THYAO",
      "companyName": "Türk Hava Yolları",
      "quantity": 10,
      "averagePrice": 600.00,
      "currentPrice": 650.00,
      "totalCost": 6000.00,
      "currentValue": 6500.00,
      "gainLoss": 500.00,
      "gainLossPercent": 8.33
    }
  ],
  "disclaimer": "..."
}
```

### POST /portfolio/items
Portföye hisse ekler. Yetersiz bakiyede 400 döner.

**Request:** `{ "symbol": "THYAO", "quantity": 10 }`

### DELETE /portfolio/items/{id}
Portföyden hisse çıkarır. Güncel fiyat kadar bakiyeyi iade eder.

---

## Dersler

### GET /lessons
Tüm eğitim derslerini sıralı döner.

### GET /lessons/{slug}
Tek ders detayı. Örn: `/lessons/borsa-nedir`

---

## Hata Yanıtları

```json
{
  "status": 404,
  "message": "Hisse bulunamadı: XYZ",
  "timestamp": "2026-05-12T10:00:00"
}
```

Stack trace hiçbir zaman yanıta dahil edilmez.
