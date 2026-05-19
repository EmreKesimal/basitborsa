import json
import logging
from pathlib import Path

from fastapi import APIRouter, Depends

from app.core.security import verify_internal_api_key
from app.schemas.ai_requests import (
    ChartStoryRequest,
    ExplainEventRequest,
    ExplainStockRequest,
    ExplainTermRequest,
)
from app.schemas.ai_responses import (
    AiExplanationResponse,
    ChartStoryResponse,
    TermExplanationResponse,
)
from app.services import (
    fallback_service,
    formatter_service,
    gemini_service,
    prompt_service,
    safety_service,
)

log = logging.getLogger(__name__)
router = APIRouter(prefix="/ai", tags=["AI"])


def _load_terms():
    path = Path(__file__).resolve().parents[1] / "knowledge_base" / "terms.json"
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def _find_term(term: str):
    normalized = term.strip().lower()
    for key, item in _load_terms().items():
        aliases = [a.lower() for a in item.get("aliases", [])]
        if normalized == key.lower() or normalized in aliases:
            return item
    return None


@router.post("/chart-story", response_model=ChartStoryResponse)
def chart_story(request: ChartStoryRequest, _: bool = Depends(verify_internal_api_key)):
    price_context = request.priceContext or {}
    clicked_date = getattr(request, "clickedDate", None)
    company_articles = [a.dict() for a in (request.companyArticles or [])]
    sector_articles = [a.dict() for a in (request.sectorArticles or [])]
    articles_available = bool(request.articlesAvailable) or bool(company_articles) or bool(sector_articles)

    prompt = prompt_service.build_chart_story_prompt(
        symbol=request.symbol,
        company_name=request.companyName,
        sector=request.sector,
        clicked_date=clicked_date,
        price_context=price_context,
        company_articles=company_articles,
        sector_articles=sector_articles,
        articles_available=articles_available,
    )

    raw, gemini_err = gemini_service.generate(prompt)

    if not raw:
        log.warning(
            "[chart-story] FALLBACK — symbol=%s reason=%s",
            request.symbol,
            gemini_err or "empty response",
        )
        return fallback_service.chart_story_fallback(request.symbol)

    if not safety_service.is_safe(raw):
        log.warning("[chart-story] FALLBACK — symbol=%s reason=safety_check_failed", request.symbol)
        return fallback_service.chart_story_fallback(request.symbol)

    result = formatter_service.parse_chart_story(raw, request.symbol)

    # Attach the real articles + their data sources to the response so frontend can render them.
    relevant: list = []
    sources: set = set()
    for a in company_articles + sector_articles:
        relevant.append({
            "title": a.get("title"),
            "sourceName": a.get("sourceName"),
            "url": a.get("url"),
            "publishedAt": a.get("publishedAt"),
            "dataSource": a.get("dataSource"),
        })
        if a.get("dataSource"):
            sources.add(a.get("dataSource"))
    price_ds = price_context.get("dataSource") if isinstance(price_context, dict) else None
    if price_ds:
        sources.add(price_ds)

    result_dict = result.dict()
    result_dict["relevantNews"] = relevant
    result_dict["sourcesUsed"] = sorted(sources)
    final = ChartStoryResponse(**result_dict)

    log.info(
        "[chart-story] OK — symbol=%s sourceType=%s articles=%d articlesAvailable=%s",
        request.symbol,
        final.sourceType,
        len(relevant),
        articles_available,
    )
    return final


@router.post("/explain-term", response_model=TermExplanationResponse)
def explain_term(request: ExplainTermRequest, _: bool = Depends(verify_internal_api_key)):
    term_data = _find_term(request.term)
    if term_data:
        return TermExplanationResponse(
            term=term_data["title"],
            simpleExplanation=term_data["simpleExplanation"],
            whyItMatters=term_data["whyItMatters"],
            example=term_data.get("example"),
            warning=term_data["warning"],
            sourceType="knowledge_base",
        )

    prompt = prompt_service.build_term_prompt(request.term)
    raw, gemini_err = gemini_service.generate(prompt)

    if not raw:
        log.warning("[explain-term] FALLBACK — term=%s reason=%s", request.term, gemini_err)
        return fallback_service.term_fallback(request.term)

    if not safety_service.is_safe(raw):
        log.warning("[explain-term] FALLBACK — term=%s reason=safety_check_failed", request.term)
        return fallback_service.term_fallback(request.term)

    lines = [l.strip() for l in raw.split("\n") if l.strip()]
    simple = next((l.replace("Açıklama:", "").strip() for l in lines if l.startswith("Açıklama:")), lines[0] if lines else "")
    why = next((l.replace("Neden önemli:", "").strip() for l in lines if l.startswith("Neden önemli:")), "")
    example = next((l.replace("Örnek:", "").strip() for l in lines if l.startswith("Örnek:")), None)

    return TermExplanationResponse(
        term=request.term,
        simpleExplanation=simple,
        whyItMatters=why,
        example=example,
        warning="Bu açıklama yatırım tavsiyesi değildir.",
        sourceType="ai_generated",
    )


@router.post("/explain-event", response_model=AiExplanationResponse)
def explain_event(request: ExplainEventRequest, _: bool = Depends(verify_internal_api_key)):
    prompt = prompt_service.build_event_prompt(
        symbol=request.symbol,
        company_name=request.companyName,
        sector=request.sector,
        event_date=request.eventDate,
        price_change_percent=request.priceChangePercent,
        event_title=request.eventTitle,
        related_news=request.relatedNews,
    )
    raw, gemini_err = gemini_service.generate(prompt)
    if not raw:
        log.warning("[explain-event] FALLBACK — symbol=%s reason=%s", request.symbol, gemini_err)
        return fallback_service.explanation_fallback()
    if not safety_service.is_safe(raw):
        return fallback_service.explanation_fallback()
    return formatter_service.parse_explanation(raw)


@router.post("/explain-stock", response_model=AiExplanationResponse)
def explain_stock(request: ExplainStockRequest, _: bool = Depends(verify_internal_api_key)):
    question = request.question or "Bu şirketi basitçe anlat"
    prompt = prompt_service.build_stock_prompt(
        symbol=request.symbol,
        company_name=request.companyName,
        sector=request.sector,
        question=question,
    )
    raw, gemini_err = gemini_service.generate(prompt)
    if not raw:
        log.warning("[explain-stock] FALLBACK — symbol=%s reason=%s", request.symbol, gemini_err)
        return fallback_service.explanation_fallback()
    if not safety_service.is_safe(raw):
        return fallback_service.explanation_fallback()
    return formatter_service.parse_explanation(raw)
