import os
from fastapi import Header, HTTPException


def verify_internal_api_key(x_internal_api_key: str = Header(default=None)):
    expected_key = os.getenv("INTERNAL_AI_API_KEY", os.getenv("INTERNAL_API_KEY", "local-dev-secret"))

    if not x_internal_api_key or x_internal_api_key != expected_key:
        raise HTTPException(status_code=401, detail="Unauthorized")

    return True
