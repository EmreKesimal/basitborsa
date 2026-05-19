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
- Sana verilmemiş haber/olay üretme. Sana verilmediyse "doğrulanmış ilgili haber bulunamadı" de.
- Cevabın sonunda yatırım tavsiyesi değildir uyarısı olmalı."""


def _format_articles(items: List[Dict[str, Any]]) -> str:
    if not items:
        return "Yok"
    out = []
    for i, a in enumerate(items, 1):
        title = (a.get("title") or "").strip()
        snippet = (a.get("snippet") or "").strip()
        source = (a.get("sourceName") or "Bilinmiyor").strip()
        published = (a.get("publishedAt") or "").strip()
        ds = (a.get("dataSource") or "").strip()
        url = (a.get("url") or "").strip()
        line = f"  {i}) [{published} | {source} | {ds}] {title}"
        if snippet:
            line += f"\n     Özet: {snippet}"
        if url:
            line += f"\n     Kaynak: {url}"
        out.append(line)
    return "\n".join(out)


def build_chart_story_prompt(
    symbol: str,
    company_name: str,
    sector: Optional[str],
    clicked_date: Optional[str],
    price_context: Optional[Dict[str, Any]],
    company_articles: List[Dict[str, Any]],
    sector_articles: List[Dict[str, Any]],
    articles_available: bool,
    # legacy args kept for backward compat
    event_date: Optional[str] = None,
    event_title: Optional[str] = None,
    price_change_percent: Optional[float] = None,
    related_news: Optional[List[str]] = None,
) -> str:
    pc = price_context or {}
    nearest = pc.get("nearestPoint") or {}
    price_lines = []
    if pc.get("latestClose") is not None:
        price_lines.append(f"Son kapanış: {pc.get('latestClose')}")
    if pc.get("changePercent") is not None:
        price_lines.append(f"30 günlük değişim: %{pc.get('changePercent'):.2f}")
    if pc.get("highestPrice") is not None and pc.get("lowestPrice") is not None:
        price_lines.append(f"30 gün aralık: {pc.get('lowestPrice')} – {pc.get('highestPrice')}")
    if pc.get("volumeTrend"):
        price_lines.append(f"Hacim eğilimi: {pc.get('volumeTrend')}")
    if pc.get("dataSource"):
        price_lines.append(f"Veri kaynağı: {pc.get('dataSource')}")
    if nearest:
        price_lines.append(
            f"Tıklanan tarihe en yakın nokta: {nearest.get('date')} kapanış {nearest.get('close')}"
        )
    price_text = "\n".join(price_lines) if price_lines else "Mevcut değil"

    company_block = _format_articles(company_articles or [])
    sector_block = _format_articles(sector_articles or [])

    no_news_directive = ""
    if not articles_available:
        no_news_directive = (
            "\nÖNEMLİ: Bu hisse için doğrulanmış ilgili haber bulunamadı. "
            "Olası bir haber/olay uydurma. 'Aynı dönemde hangi gelişmeler vardı?' bölümünde "
            "'Doğrulanmış ilgili haber bulunamadı' yaz ve fiyat hareketini sadece eğitim amaçlı, "
            "ihtiyatlı ifadelerle yorumla."
        )

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
Kullanıcının grafikte tıkladığı tarih: {clicked_date or "belirtilmemiş"}

Fiyat bağlamı:
{price_text}

Şirket haberleri (gerçek kaynaklardan, tıklanan tarihten önce):
{company_block}

Sektör haberleri (gerçek kaynaklardan, tıklanan tarihten önce):
{sector_block}
{no_news_directive}

Sadece yukarıda verilen haberleri kullan. Verilmeyen olay/haber üretme.
Bölüm 2'de en fazla 3 haberi adıyla ve tarihiyle özetle.
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
