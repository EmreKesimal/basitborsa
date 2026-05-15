import re
from typing import List, Optional

from app.schemas.ai_responses import ChartStoryResponse, ChartStorySection, AiExplanationResponse

DISCLAIMER = "Bu açıklama yatırım tavsiyesi değildir."

# The four required section titles and their numbered variants Gemini may use
SECTION_TITLES = [
    "Grafikte ne oldu?",
    "Aynı dönemde hangi gelişmeler vardı?",
    "Bu gelişmeler fiyat hareketiyle nasıl ilişkili olabilir?",
    "Yeni başlayan biri buradan ne öğrenmeli?",
]

# Patterns Gemini commonly prefixes section titles with
_PREFIX = r"(?:(?:\*{1,2})?(?:\d+[\.\)]\s*)?(?:\*{1,2})?)"


def _strip_markdown(text: str) -> str:
    """Remove leading/trailing asterisks, hashes, and numbering from a line."""
    text = re.sub(r"^\*{1,3}|^\#{1,6}\s*|\*{1,3}$", "", text.strip())
    text = re.sub(r"^\d+[\.\)]\s*", "", text.strip())
    return text.strip()


def _extract_section(raw: str, title: str, next_titles: List[str]) -> Optional[str]:
    """
    Find the content block that follows `title` in `raw`.
    Handles:
      - "Grafikte ne oldu?\nContent"
      - "**1. Grafikte ne oldu?**\nContent"
      - "1. Grafikte ne oldu?: Content"
    """
    # Build a flexible pattern: optional markup + optional number + title keyword
    escaped = re.escape(title)
    # lookahead: next section title or end of string
    lookahead_parts = [re.escape(t) for t in next_titles]
    lookahead = "(?=" + "|".join(lookahead_parts) + "|$)" if lookahead_parts else "(?=$)"

    pattern = re.compile(
        _PREFIX + escaped + r"[:\*\*]*\s*\n?(.*?)" + lookahead,
        re.DOTALL | re.IGNORECASE,
    )
    m = pattern.search(raw)
    if not m:
        return None

    content = m.group(1).strip()
    # Strip trailing ** or ## noise
    content = re.sub(r"\*{1,3}$", "", content).strip()
    # Remove blank lines and clean each line
    lines = [l.strip() for l in content.split("\n") if l.strip()]
    lines = [_strip_markdown(l) if l.startswith("*") or l.startswith("#") else l for l in lines]
    return " ".join(lines) if lines else None


def _extract_summary(raw: str, symbol: str) -> str:
    """First substantive line that doesn't look like a section header."""
    for line in raw.split("\n"):
        line = line.strip()
        if not line:
            continue
        cleaned = _strip_markdown(line)
        if not cleaned:
            continue
        # Skip lines that are just section titles
        if any(t.lower() in cleaned.lower() for t in SECTION_TITLES):
            continue
        return cleaned
    return f"{symbol} için fiyat hareketi değerlendirmesi."


def parse_chart_story(raw: str, symbol: str = "") -> ChartStoryResponse:
    sections: List[ChartStorySection] = []

    for i, title in enumerate(SECTION_TITLES):
        # All other titles are potential "next" boundaries
        next_titles = [t for j, t in enumerate(SECTION_TITLES) if j != i]
        content = _extract_section(raw, title, next_titles)
        sections.append(ChartStorySection(
            title=title,
            content=content or "Bilgi mevcut değil.",
        ))

    summary = _extract_summary(raw, symbol)

    return ChartStoryResponse(
        summary=summary,
        sections=sections,
        warnings=[DISCLAIMER],
        sourceType="ai_generated",
        safetyPassed=True,
    )


def parse_explanation(raw: str) -> AiExplanationResponse:
    lines = [l.strip() for l in raw.split("\n") if l.strip()]
    summary = _strip_markdown(lines[0]) if lines else "Açıklama mevcut değil."
    factors = [
        "Bu gelişmeler fiyat hareketine katkı sağlamış olabilir.",
        "Piyasa beklentileri ve genel ekonomik koşullar da etkili olmuş olabilir.",
    ]
    learning_note = (
        "Hisseleri etkileyen tek bir faktör olmayabilir. "
        "Farklı açılardan değerlendirmek önemlidir."
    )
    return AiExplanationResponse(
        summary=summary,
        possibleFactors=factors,
        learningNote=learning_note,
        disclaimer="Bu açıklama yatırım tavsiyesi değildir. Eğitim ve demo amaçlıdır.",
        sourceType="ai_generated",
        safetyPassed=True,
    )
