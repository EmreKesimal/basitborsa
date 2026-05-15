from pydantic import BaseModel
from typing import Any, Dict, List, Optional


class SelectedEvent(BaseModel):
    eventDate: str
    eventTitle: str
    priceChangePercent: float = 0.0
    relatedNews: List[str] = []
    learningNote: Optional[str] = None


class PriceContext(BaseModel):
    range: str = "30d"
    latestClose: Optional[float] = None
    previousClose: Optional[float] = None
    changePercent: Optional[float] = None
    highestPrice: Optional[float] = None
    lowestPrice: Optional[float] = None
    volumeTrend: str = "normal"
    dataSource: str = "SEED"


class ChartStoryRequest(BaseModel):
    symbol: str
    companyName: str
    sector: Optional[str] = None
    priceContext: Optional[Dict[str, Any]] = None
    selectedEvent: Optional[SelectedEvent] = None
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
