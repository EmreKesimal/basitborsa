from typing import Any, Dict, List, Optional

SYSTEM_PREAMBLE = """Sen bir yatırım danışmanı değilsin.
Yeni başlayan kullanıcılara borsayı öğretmek için sade açıklamalar yapan bir finansal okuryazarlık asistanısın.

Kurallar:
- Kesin neden-sonuç ilişkisi kurma.
- Al/sat önerisi verme.
- Fiyat tahmini yapma.
- "Olabilir", "katkı sağlamış olabilir", "dikkat çekiyor" gibi ihtiyatlı ifadeler kullan.
- Cevabı kısa, sade ve eğitim odaklı ver.
- Yeni başlayan bir kullanıcının anlayacağı dil kullan.
- Cevabın sonunda yatırım tavsiyesi değildir uyarısı olmalı."""


def build_chart_story_prompt(
    symbol: str,
    company_name: str,
    sector: Optional[str],
    event_date: Optional[str],
    event_title: Optional[str],
    price_change_percent: Optional[float],
    related_news: List[str],
    price_context: Optional[Dict[str, Any]],
) -> str:
    news_text = "; ".join(related_news) if related_news else "Bilgi yok"
    price_text = ""
    if price_context:
        latest = price_context.get("latestClose")
        change = price_context.get("changePercent")
        highest = price_context.get("highestPrice")
        lowest = price_context.get("lowestPrice")
        if latest:
            price_text = f"Son kapanış: {latest}"
        if change is not None:
            price_text += f", 30 günlük değişim: %{change:.2f}"
        if highest and lowest:
            price_text += f", 30 günlük en yüksek: {highest}, en düşük: {lowest}"

    return f"""{SYSTEM_PREAMBLE}

Cevap formatı (her bölümü ayrı başlık ile ver):
1. Grafikte ne oldu?
2. Aynı dönemde hangi gelişmeler vardı?
3. Bu gelişmeler fiyat hareketiyle nasıl ilişkili olabilir?
4. Yeni başlayan biri buradan ne öğrenmeli?
5. Yatırım tavsiyesi değildir uyarısı.

Veriler:
Hisse: {symbol}
Şirket: {company_name}
Sektör: {sector or "Bilinmiyor"}
Tarih: {event_date or "Bilinmiyor"}
Fiyat değişimi: {f"%{price_change_percent:.1f}" if price_change_percent is not None else "Bilinmiyor"}
Olay: {event_title or "Bilinmiyor"}
İlgili haberler: {news_text}
Fiyat bağlamı: {price_text or "Mevcut değil"}

Her bölümü net başlıkla yaz. Kısa ve anlaşılır tut."""


def build_term_prompt(term: str) -> str:
    return f"""{SYSTEM_PREAMBLE}

"{term}" terimini yeni başlayan bir yatırımcıya 2-3 cümleyle sade Türkçe ile açıkla.
Teknik jargondan kaçın. Gerçek bir örnek ver.
Format:
Açıklama: ...
Neden önemli: ...
Örnek: ...
Uyarı: Bu açıklama yatırım tavsiyesi değildir."""


def build_stock_prompt(symbol: str, company_name: str, sector: Optional[str], question: str) -> str:
    return f"""{SYSTEM_PREAMBLE}

Hisse: {symbol} ({company_name})
Sektör: {sector or "Bilinmiyor"}

Kullanıcı sorusu: {question}

Kısa, sade, eğitim odaklı cevap ver. Al/sat önerisi verme. Kesin tahmin yapma.
Cevabın sonuna: "Bu açıklama yatırım tavsiyesi değildir." ekle."""


def build_event_prompt(
    symbol: str,
    company_name: str,
    sector: Optional[str],
    event_date: str,
    price_change_percent: Optional[float],
    event_title: str,
    related_news: List[str],
) -> str:
    news_text = "; ".join(related_news) if related_news else "Bilgi yok"
    return f"""{SYSTEM_PREAMBLE}

Hisse: {symbol}
Şirket: {company_name}
Sektör: {sector or "Bilinmiyor"}
Tarih: {event_date}
Fiyat değişimi: {f"%{price_change_percent:.1f}" if price_change_percent is not None else "Bilinmiyor"}
Olay: {event_title}
İlgili haberler: {news_text}

Kısa, anlaşılır ve eğitim odaklı bir açıklama yap. Yatırım tavsiyesi verme."""
