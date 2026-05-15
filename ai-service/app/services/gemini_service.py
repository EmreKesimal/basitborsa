import logging
from typing import Optional, Tuple

from app.core.config import GEMINI_API_KEY, GEMINI_MODEL, GEMINI_FALLBACK_MODEL

log = logging.getLogger(__name__)

_clients: dict = {}  # model_name → GenerativeModel


def _get_client(model: str) -> Tuple[object, Optional[str]]:
    """Returns (client_or_None, error_reason_or_None)."""
    if model in _clients:
        return _clients[model], None

    if not GEMINI_API_KEY:
        reason = "GEMINI_API_KEY is not set or empty"
        log.warning("[gemini] %s", reason)
        return None, reason

    try:
        import google.generativeai as genai
    except ImportError:
        reason = "google-generativeai package not installed — run: pip install google-generativeai"
        log.error("[gemini] %s", reason)
        return None, reason

    try:
        genai.configure(api_key=GEMINI_API_KEY)
        client = genai.GenerativeModel(model)
        _clients[model] = client
        log.info("[gemini] Client ready — model=%s", model)
        return client, None
    except Exception as e:
        reason = f"GenerativeModel init failed for {model}: {e}"
        log.error("[gemini] %s", reason, exc_info=True)
        return None, reason


def _call_model(model: str, prompt: str) -> Tuple[Optional[str], Optional[str]]:
    """Returns (text, error_reason). Evicts cached client on quota/auth errors."""
    client, init_err = _get_client(model)
    if client is None:
        return None, init_err or "client unavailable"

    try:
        response = client.generate_content(prompt)

        if not response.candidates:
            reason = f"model={model} returned no candidates (prompt may have been blocked)"
            log.warning("[gemini] %s", reason)
            return None, reason

        text = response.text
        if not text or not text.strip():
            reason = f"model={model} returned empty text"
            log.warning("[gemini] %s", reason)
            return None, reason

        log.debug("[gemini] model=%s generated %d chars", model, len(text))
        return text, None

    except Exception as e:
        err_str = str(e)
        # Evict cached client so next call retries (handles transient errors)
        _clients.pop(model, None)

        is_quota = "429" in err_str or "ResourceExhausted" in type(e).__name__ or "quota" in err_str.lower() or "credits" in err_str.lower()
        is_not_found = "404" in err_str or "NotFound" in type(e).__name__

        if is_quota:
            reason = (
                f"model={model} quota/billing error — "
                "check https://ai.google.dev/gemini-api/docs/billing — "
                f"detail={err_str[:120]}"
            )
            log.warning("[gemini] %s", reason)
        elif is_not_found:
            reason = f"model={model} not found — check GEMINI_MODEL env var"
            log.warning("[gemini] %s — detail=%s", reason, err_str[:120])
        else:
            reason = f"model={model} generate_content failed: {err_str[:200]}"
            log.error("[gemini] %s", reason, exc_info=True)

        return None, reason


def generate(prompt: str) -> Tuple[Optional[str], Optional[str]]:
    """
    Returns (text_or_None, fallback_reason_or_None).

    Tries GEMINI_MODEL first.
    On quota/billing failure, retries with GEMINI_FALLBACK_MODEL if different.
    Callers must unpack both values: text, err = generate(prompt)
    """
    text, err = _call_model(GEMINI_MODEL, prompt)
    if text:
        return text, None

    # Retry with fallback model if primary failed and fallback is different
    if GEMINI_FALLBACK_MODEL and GEMINI_FALLBACK_MODEL != GEMINI_MODEL:
        log.info("[gemini] Retrying with fallback model=%s", GEMINI_FALLBACK_MODEL)
        text, fallback_err = _call_model(GEMINI_FALLBACK_MODEL, prompt)
        if text:
            return text, None
        return None, f"primary=({err}) fallback=({fallback_err})"

    return None, err


def check_availability() -> dict:
    """Availability check — does NOT make an API call."""
    if not GEMINI_API_KEY:
        return {"available": False, "reason": "GEMINI_API_KEY not set"}
    try:
        import google.generativeai as genai  # noqa: F401
        return {
            "available": True,
            "primaryModel": GEMINI_MODEL,
            "fallbackModel": GEMINI_FALLBACK_MODEL,
            "key_set": True,
        }
    except ImportError:
        return {"available": False, "reason": "google-generativeai not installed"}
