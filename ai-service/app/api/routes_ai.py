from fastapi import APIRouter
from pydantic import BaseModel
from typing import List, Optional
import json
from pathlib import Path

router = APIRouter(prefix="/ai", tags=["AI"])


class SelectedEvent(BaseModel):
    eventDate: str
    eventTitle: str
    priceChangePercent: float
    relatedNews: List[str] = []


class ChartStoryRequest(BaseModel):
    symbol: str
    companyName: str
    sector: Optional[str] = None
    selectedEvent: SelectedEvent
    userLevel: str = "beginner"
    language: str = "tr"


class TermExplanationRequest(BaseModel):
    term: str
    userLevel: str = "beginner"
    language: str = "tr"


def load_terms():
    terms_path = Path(__file__).resolve().parents[1] / "knowledge_base" / "terms.json"
    with open(terms_path, "r", encoding="utf-8") as file:
        return json.load(file)


def find_term(term: str):
    normalized = term.strip().lower()
    terms = load_terms()

    for key, item in terms.items():
        aliases = [alias.lower() for alias in item.get("aliases", [])]
        if normalized == key.lower() or normalized in aliases:
            return item

    return None


@router.post("/chart-story")
def chart_story(request: ChartStoryRequest):
    return {
        "summary": f"{request.symbol} için seçilen dönemde dikkat çeken bir fiyat hareketi görülüyor.",
        "sections": [
            {
                "title": "Grafikte ne oldu?",
                "content": f"{request.selectedEvent.eventTitle} döneminde fiyat değişimi dikkat çekiyor."
            },
            {
                "title": "Aynı dönemde hangi gelişmeler vardı?",
                "content": "İlgili haberler ve sektör gelişmeleri bu dönemde yatırımcı algısını etkilemiş olabilir."
            },
            {
                "title": "Bu gelişmeler fiyat hareketiyle nasıl ilişkili olabilir?",
                "content": "Bu gelişmeler fiyat hareketine katkı sağlamış olabilir, ancak fiyat hareketlerinin tek ve kesin nedeni olmayabilir."
            },
            {
                "title": "Yeni başlayan biri buradan ne öğrenmeli?",
                "content": "Hisse fiyatları şirket haberleri, sektör beklentileri ve genel piyasa koşullarıyla birlikte değerlendirilmelidir."
            }
        ],
        "warnings": [
            "Bu açıklama yatırım tavsiyesi değildir."
        ],
        "sourceType": "fallback",
        "safetyPassed": True
    }


@router.post("/explain-term")
def explain_term(request: TermExplanationRequest):
    term_data = find_term(request.term)

    if term_data:
        return {
            "term": term_data["title"],
            "simpleExplanation": term_data["simpleExplanation"],
            "whyItMatters": term_data["whyItMatters"],
            "example": term_data.get("example"),
            "warning": term_data["warning"],
            "sourceType": "knowledge_base"
        }

    return {
        "term": request.term,
        "simpleExplanation": "Bu kavram için yerel bilgi tabanında hazır bir açıklama bulunamadı.",
        "whyItMatters": "Finansal kavramları öğrenmek, grafik ve şirket bilgilerini daha bilinçli yorumlamaya yardımcı olur.",
        "example": None,
        "warning": "Bu açıklama yatırım tavsiyesi değildir.",
        "sourceType": "fallback"
    }
