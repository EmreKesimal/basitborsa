from pydantic import BaseModel
from typing import Any, Dict, List, Optional


class SelectedEvent(BaseModel):
    """Legacy field — kept for backward compatibility, no longer used by chart-story."""
    eventDate: Optional[str] = None
    eventTitle: Optional[str] = None
    priceChangePercent: float = 0.0
    relatedNews: List[str] = []
    learningNote: Optional[str] = None


class NewsArticle(BaseModel):
    title: str
    snippet: Optional[str] = None
    sourceName: Optional[str] = None
    url: Optional[str] = None
    publishedAt: Optional[str] = None
    dataSource: Optional[str] = None  # EXTERNAL_NEWS | KAP | CACHED_NEWS
    relation: Optional[str] = None     # COMPANY | SECTOR


class PriceContext(BaseModel):
    range: str = "30d"
    latestClose: Optional[float] = None
    previousClose: Optional[float] = None
    changePercent: Optional[float] = None
    highestPrice: Optional[float] = None
    lowestPrice: Optional[float] = None
    volumeTrend: str = "normal"
    dataSource: str = "EXTERNAL_PROVIDER"
    nearestPoint: Optional[Dict[str, Any]] = None


class ChartStoryRequest(BaseModel):
    symbol: str
    companyName: str
    sector: Optional[str] = None
    priceContext: Optional[Dict[str, Any]] = None
    selectedEvent: Optional[SelectedEvent] = None  # legacy, ignored if articles present
    clickedDate: Optional[str] = None
    companyArticles: List[NewsArticle] = []
    sectorArticles: List[NewsArticle] = []
    articlesAvailable: bool = False
    userLevel: str = "beginner"
    language: str = "tr"


class ExplainTermRequest(BaseModel):
    term: str
    userLevel: str = "beginner"
    language: str = "tr"


class ExplainEventRequest(BaseModel):
    symbol: str
    companyName: str
    sector: Optional[str] = None
    eventDate: str
    priceChangePercent: Optional[float] = None
    eventTitle: str
    relatedNews: List[str] = []
    userLevel: str = "beginner"
    language: str = "tr"


class ExplainStockRequest(BaseModel):
    symbol: str
    companyName: str
    sector: Optional[str] = None
    question: Optional[str] = None
    userLevel: str = "beginner"
    language: str = "tr"
