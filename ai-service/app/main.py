from fastapi import FastAPI
from app.api.routes_health import router as health_router
from app.api.routes_ai import router as ai_router

app = FastAPI(title="BasitBorsa AI Service")

app.include_router(health_router)
app.include_router(ai_router)
