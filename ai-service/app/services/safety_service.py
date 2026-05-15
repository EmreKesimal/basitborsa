import logging

log = logging.getLogger(__name__)

FORBIDDEN_PHRASES = [
    "bu yüzden yükseldi",
    "bu hisse alınır",
    "kesin yükselecek",
    "kazandırır",
    "alım fırsatı",
    "satılmalı",
    "alınmalı",
    "bu hisseyi al",
    "bu hisseyi sat",
    "garantili kazanç",
    "kesinlikle kazandırır",
    "bu fiyattan al",
    "stop-loss",
    "buy the dip",
]


def is_safe(text: str) -> bool:
    if not text:
        return True
    lower = text.lower()
    for phrase in FORBIDDEN_PHRASES:
        if phrase in lower:
            log.warning("Safety check failed — forbidden phrase found: '%s'", phrase)
            return False
    return True


def sanitize(text: str) -> str:
    if not text:
        return text
    lower = text.lower()
    for phrase in FORBIDDEN_PHRASES:
        if phrase in lower:
            idx = lower.find(phrase)
            text = text[:idx] + "[...] " + text[idx + len(phrase):]
            lower = text.lower()
    return text
