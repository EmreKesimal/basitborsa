import os
from dotenv import load_dotenv

load_dotenv()

GEMINI_API_KEY: str = os.getenv("GEMINI_API_KEY", "")
INTERNAL_AI_API_KEY: str = os.getenv("INTERNAL_AI_API_KEY", "local-dev-secret")

# Primary model — gemini-2.0-flash is free tier but may need billing for heavy use
# If primary hits quota (429), service retries with fallback model
GEMINI_MODEL: str = os.getenv("GEMINI_MODEL", "gemini-2.0-flash")
GEMINI_FALLBACK_MODEL: str = os.getenv("GEMINI_FALLBACK_MODEL", "gemini-2.0-flash-lite")
