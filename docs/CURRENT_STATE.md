# Current State

## Done
- Twelve Data real market data works through Spring Boot.
- Python AI service runs.
- Internal API key works.
- GEMINI_API_KEY loads correctly.
- Direct Gemini REST test works with gemini-2.5-flash-lite.
- Direct Python /ai/chart-story returns sourceType=ai_generated.
- /ai/explain-term works.
- Fallback behavior works safely.

## Important Discovery
Use gemini-2.5-flash-lite.

Do not use:
- gemini-2.0-flash because current key gets 429 quota.
- gemini-1.5-flash because current endpoint returns 404.

## Next
- Verify Spring /api/ai/chart-story returns sourceType=ai_generated.
- If fallback, debug Spring -> Python payload/config.
- Polish cautious AI wording.
- Update README and .env.example.
- Keep ruflo files untouched.