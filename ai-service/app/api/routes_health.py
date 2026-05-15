from fastapi import APIRouter
from app.services.gemini_service import check_availability

router = APIRouter()


@router.get("/health")
def health_check():
    return {"status": "ok", "service": "basitborsa-ai-service"}


@router.get("/health/gemini")
def gemini_health():
    """Returns Gemini availability without making an API call."""
    return check_availability()
