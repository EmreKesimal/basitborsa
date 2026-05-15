#!/usr/bin/env python3
"""
Direct smoke test for Gemini call.
Run from ai-service/ dir with Python 3.11+:
    python3.11 smoke_test_gemini.py

Does NOT call the FastAPI service — tests the Gemini layer directly.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(__file__))

from dotenv import load_dotenv
load_dotenv()

from app.core.config import GEMINI_API_KEY, GEMINI_MODEL, GEMINI_FALLBACK_MODEL
from app.services.gemini_service import check_availability, generate
from app.services.formatter_service import parse_chart_story
from app.services import safety_service

print("=== Gemini Smoke Test ===")
print(f"Primary model  : {GEMINI_MODEL}")
print(f"Fallback model : {GEMINI_FALLBACK_MODEL}")
print(f"Key set        : {bool(GEMINI_API_KEY)}")
print()

status = check_availability()
print("Availability check:", status)
if not status.get("available"):
    print("\nFAIL — Gemini not available:", status.get("reason"))
    if "not installed" in status.get("reason", ""):
        print("Fix: pip install google-generativeai")
    sys.exit(1)

print("\n--- Calling generate() ---")
PROMPT = """Sen bir finansal okuryazarlık asistanısın.
THYAO hissesi için kısa bir chart story yaz.
Şu 4 bölümü TAM BAŞLIKLARLA yaz:

1. Grafikte ne oldu?
2. Aynı dönemde hangi gelişmeler vardı?
3. Bu gelişmeler fiyat hareketiyle nasıl ilişkili olabilir?
4. Yeni başlayan biri buradan ne öğrenmeli?

Kısa tut. Yatırım tavsiyesi verme."""

raw, err = generate(PROMPT)
if not raw:
    print(f"\nFAIL — generate returned None")
    print(f"Reason: {err}")
    if "429" in (err or "") or "credits" in (err or "").lower() or "quota" in (err or "").lower():
        print("""
BILLING FIX REQUIRED:
  Your Gemini prepaid credits are depleted.
  Options:
    1. Go to https://aistudio.google.com → create a NEW project (no billing)
       → generate a new API key → set GEMINI_API_KEY=<new-key>
    2. OR top up credits at https://ai.studio/projects
  Set GEMINI_MODEL=gemini-2.0-flash in .env (free tier: 1500 req/day).
""")
    sys.exit(1)

print(f"OK — got {len(raw)} chars from model")
print("\n--- Raw output (first 400 chars) ---")
print(raw[:400])

print("\n--- Safety check ---")
safe = safety_service.is_safe(raw)
print("Safe:", safe)
if not safe:
    print("WARNING: forbidden phrase detected — would return fallback in production")

print("\n--- Parsing ---")
result = parse_chart_story(raw, "THYAO")
print("sourceType:", result.sourceType)
print("summary   :", result.summary[:100])
all_parsed = all(s.content != "Bilgi mevcut değil." for s in result.sections)
for s in result.sections:
    status_tag = "OK" if s.content != "Bilgi mevcut değil." else "EMPTY"
    print(f"  [{status_tag}] {s.title}")

if not all_parsed:
    print("\nWARNING: some sections empty — Gemini may have used unexpected formatting")
    print("This is OK — sections show 'Bilgi mevcut değil.' rather than crashing")

print("\n=== PASS ===")
