import { useState } from 'react'
import { aiService } from '../../services/aiService.js'

const PRESET_QUESTIONS = [
  'Bu şirketi bana basitçe anlat',
  'Bu grafikte ne olmuş?',
  'F/K nedir?',
  'PD/DD nedir?',
  'Temettü nedir?',
  'Volatilite nedir?',
  'Bu hisseyi incelerken nelere dikkat etmeliyim?',
]

export default function AiTeacherBox({ stock }) {
  const [question, setQuestion] = useState('')
  const [response, setResponse] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  async function ask(q) {
    const finalQ = q || question
    if (!finalQ.trim()) return
    setLoading(true)
    setError(null)
    setResponse(null)
    try {
      const res = await aiService.explainStock(stock.symbol, stock.companyName, stock.sector, finalQ)
      setResponse(res)
    } catch (err) {
      setError('Yapay zekâ yanıtı alınamadı. Lütfen tekrar deneyin.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card p-stack-gap-md flex flex-col gap-stack-gap-sm">
      <div className="flex items-center gap-unit">
        <span className="material-symbols-outlined text-primary text-[22px]" style={{ fontVariationSettings: "'FILL' 1" }}>
          smart_toy
        </span>
        <h3 className="text-headline-md font-semibold text-on-surface">AI Öğretmen</h3>
      </div>

      <div className="flex flex-wrap gap-2">
        {PRESET_QUESTIONS.map((q) => (
          <button
            key={q}
            onClick={() => ask(q)}
            className="text-xs bg-surface-container text-on-surface-variant px-3 py-1.5 rounded-full hover:bg-surface-container-high transition-colors border border-outline-variant"
          >
            {q}
          </button>
        ))}
      </div>

      <div className="flex gap-2">
        <input
          type="text"
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && ask()}
          placeholder="Başka bir soru sor..."
          className="flex-1 bg-surface-container-low border-none rounded-lg px-4 py-2 text-body-md text-on-surface placeholder:text-outline focus:ring-2 focus:ring-primary outline-none"
        />
        <button
          onClick={() => ask()}
          disabled={loading || !question.trim()}
          className="bg-primary text-on-primary px-4 py-2 rounded-lg font-semibold text-label-md hover:opacity-90 transition-opacity disabled:opacity-50"
        >
          <span className="material-symbols-outlined text-[18px]">send</span>
        </button>
      </div>

      {loading && (
        <div className="flex items-center gap-2 text-on-surface-variant text-body-md">
          <div className="w-4 h-4 border-2 border-primary border-t-transparent rounded-full animate-spin" />
          Yanıt hazırlanıyor...
        </div>
      )}

      {error && <p className="text-error text-body-md">{error}</p>}

      {response && !loading && (
        <div className="bg-surface-container-low rounded-xl p-stack-gap-sm flex flex-col gap-2">
          <p className="text-body-md text-on-surface">{response.summary}</p>
          {response.possibleFactors?.length > 0 && (
            <ul className="flex flex-col gap-1">
              {response.possibleFactors.map((f, i) => (
                <li key={i} className="flex items-start gap-2 text-body-md text-on-surface-variant">
                  <span className="material-symbols-outlined text-primary text-[14px] mt-0.5">check_circle</span>
                  {f}
                </li>
              ))}
            </ul>
          )}
          {response.learningNote && (
            <div className="learning-note">
              <span className="material-symbols-outlined text-tertiary-container text-[18px] mt-0.5">lightbulb</span>
              <p className="text-body-md text-on-surface-variant">{response.learningNote}</p>
            </div>
          )}
          <p className="text-xs text-outline italic">{response.disclaimer}</p>
        </div>
      )}
    </div>
  )
}
