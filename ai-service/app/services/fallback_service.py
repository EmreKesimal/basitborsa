from app.schemas.ai_responses import (
    ChartStoryResponse, ChartStorySection,
    AiExplanationResponse, TermExplanationResponse,
)

DISCLAIMER = "Bu açıklama yatırım tavsiyesi değildir."
GENERIC_DISCLAIMER = "Bu platform yatırım tavsiyesi vermez. Eğitim ve demo amaçlıdır."


def chart_story_fallback(symbol: str = "") -> ChartStoryResponse:
    return ChartStoryResponse(
        summary=f"{symbol} için seçilen dönemde dikkat çeken bir fiyat hareketi görülüyor.".strip(),
        sections=[
            ChartStorySection(
                title="Grafikte ne oldu?",
                content="Bu dönemde fiyat hareketleri dikkat çekiyor. Detaylı yapay zekâ analizi şu an kullanılamıyor.",
            ),
            ChartStorySection(
                title="Aynı dönemde hangi gelişmeler vardı?",
                content="İlgili haberler ve sektör gelişmeleri bu dönemde yatırımcı algısını etkilemiş olabilir.",
            ),
            ChartStorySection(
                title="Bu gelişmeler fiyat hareketiyle nasıl ilişkili olabilir?",
                content="Bu gelişmeler fiyat hareketine katkı sağlamış olabilir, ancak fiyat hareketlerinin tek ve kesin nedeni olmayabilir.",
            ),
            ChartStorySection(
                title="Yeni başlayan biri buradan ne öğrenmeli?",
                content="Hisse fiyatları şirket haberleri, sektör beklentileri ve genel piyasa koşullarıyla birlikte değerlendirilmelidir.",
            ),
        ],
        warnings=[DISCLAIMER],
        sourceType="fallback",
        safetyPassed=True,
    )


def explanation_fallback() -> AiExplanationResponse:
    return AiExplanationResponse(
        summary="Yapay zekâ açıklaması şu anda kullanılamıyor. Ders merkezindeki eğitim içeriklerini inceleyebilirsiniz.",
        possibleFactors=[
            "Fiyat hareketleri birden fazla faktörden etkilenebilir.",
            "Şirket haberleri, sektör gelişmeleri ve genel piyasa koşulları rol oynayabilir.",
        ],
        learningNote="Bir hisseyi anlamak için şirketin ne iş yaptığını, finansal verilerini ve sektör bağlamını birlikte değerlendirin.",
        disclaimer=GENERIC_DISCLAIMER,
        sourceType="fallback",
        safetyPassed=True,
    )


def term_fallback(term: str) -> TermExplanationResponse:
    return TermExplanationResponse(
        term=term,
        simpleExplanation="Bu kavram için hazır bir açıklama bulunamadı.",
        whyItMatters="Finansal kavramları öğrenmek, grafik ve şirket bilgilerini daha bilinçli yorumlamaya yardımcı olur.",
        example=None,
        warning=DISCLAIMER,
        sourceType="fallback",
    )
