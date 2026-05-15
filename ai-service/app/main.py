import logging

from fastapi import FastAPI

from app.api.routes_health import router as health_router
from app.api.routes_ai import router as ai_router
from app.core.config import GEMINI_API_KEY, GEMINI_MODEL
from app.services.gemini_service import check_availability

logging.basicConfig(level=logging.INFO)
log = logging.getLogger(__name__)

app = FastAPI(title="BasitBorsa AI Service")

app.include_router(health_router)
app.include_router(ai_router)


@app.on_event("startup")
def on_startup():
    status = check_availability()
    if status.get("available"):
        log.info("[startup] Gemini available — model=%s key_set=%s", GEMINI_MODEL, bool(GEMINI_API_KEY))
    else:
        log.warning("[startup] Gemini NOT available — reason=%s", status.get("reason"))
