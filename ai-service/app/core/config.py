import os
from dotenv import load_dotenv

load_dotenv()

GEMINI_API_KEY: str = os.getenv("GEMINI_API_KEY", "")
INTERNAL_AI_API_KEY: str = os.getenv("INTERNAL_AI_API_KEY", "local-dev-secret")

# Default model verified to work on current free-tier API.
# Override via GEMINI_MODEL env var. If primary hits quota (429), service retries with fallback model.
GEMINI_MODEL: str = os.getenv("GEMINI_MODEL", "gemini-2.5-flash-lite")
GEMINI_FALLBACK_MODEL: str = os.getenv("GEMINI_FALLBACK_MODEL", "gemini-2.0-flash-lite")
