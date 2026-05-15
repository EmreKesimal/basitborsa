from pydantic import BaseModel
from typing import List, Optional


class ChartStorySection(BaseModel):
    title: str
    content: str


class ChartStoryResponse(BaseModel):
    summary: str
    sections: List[ChartStorySection]
    warnings: List[str]
    sourceType: str
    safetyPassed: bool


class TermExplanationResponse(BaseModel):
    term: str
    simpleExplanation: str
    whyItMatters: str
    example: Optional[str]
    warning: str
    sourceType: str


class AiExplanationResponse(BaseModel):
    summary: str
    possibleFactors: List[str]
    learningNote: str
    disclaimer: str
    sourceType: str
    safetyPassed: bool
