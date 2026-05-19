import { useState } from 'react'
import { aiService } from '../../services/aiService.js'
import LottieAnimation from '../common/LottieAnimation.jsx'

const TERM_PRESETS = ['F/K nedir?', 'PD/DD nedir?', 'Temettü nedir?', 'Volatilite nedir?']
const STOCK_PRESETS = [
  'Bu şirketi bana basitçe anlat',
  'Bu hisseyi incelerken nelere dikkat etmeliyim?',
]

export default function AiTeacherBox({ stock }) {
  const [question, setQuestion] = useState('')
  const [response, setResponse] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  async function askTerm(term) {
    setLoading(true)
    setError(null)
    setResponse(null)
    try {
      setResponse(await aiService.explainTerm(term))
    } catch {
      setError('Yapay zekâ yanıtı alınamadı. Lütfen tekrar deneyin.')
    } finally {
      setLoading(false)
    }
  }

  async function askStock(q) {
    const finalQ = q || question
    if (!finalQ.trim()) return
    setLoading(true)
    setError(null)
    setResponse(null)
    try {
      setResponse(await aiService.explainStock(stock.symbol, stock.companyName, stock.sector, finalQ))
    } catch {
      setError('Yapay zekâ yanıtı alınamadı. Lütfen tekrar deneyin.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card p-5 flex flex-col gap-5">
      {/* Title */}
      <div className="flex items-center gap-2.5">
        <div className="w-9 h-9 rounded-xl bg-primary flex items-center justify-center flex-shrink-0">
          <span className="material-symbols-outlined text-on-primary text-[18px]" style={{ fontVariationSettings: "'FILL' 1" }}>
            smart_toy
          </span>
        </div>
        <div>
          <h3 className="text-headline-md font-bold text-on-surface">AI Öğretmen</h3>
          <p className="text-xs text-outline">Soruların için buradayım</p>
        </div>
      </div>

      {/* Term presets */}
      <div className="flex flex-col gap-2">
        <p className="text-xs text-outline uppercase tracking-wide">Terimler</p>
        <div className="flex flex-wrap gap-2">
          {TERM_PRESETS.map((q) => (
            <button
              key={q}
              onClick={() => askTerm(q)}
              disabled={loading}
              className="text-xs bg-surface-container-low text-on-surface-variant px-3 py-1.5 rounded-full border border-outline-variant hover:border-primary hover:text-primary transition-colors disabled:opacity-50"
            >
              {q}
            </button>
          ))}
        </div>
      </div>

      {/* Stock presets */}
      <div className="flex flex-col gap-2">
        <p className="text-xs text-outline uppercase tracking-wide">Bu şirket hakkında</p>
        <div className="flex flex-wrap gap-2">
          {STOCK_PRESETS.map((q) => (
            <button
              key={q}
              onClick={() => askStock(q)}
              disabled={loading}
              className="text-xs bg-surface-container-low text-on-surface-variant px-3 py-1.5 rounded-full border border-outline-variant hover:border-primary hover:text-primary transition-colors disabled:opacity-50"
            >
              {q}
            </button>
          ))}
        </div>
      </div>

      {/* Free-text input */}
      <div className="flex gap-2">
        <input
          type="text"
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && askStock()}
          placeholder="Başka bir soru sor..."
          disabled={loading}
          className="flex-1 bg-surface-container-low border border-outline-variant rounded-xl px-4 py-2.5 text-body-md text-on-surface placeholder:text-outline focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all disabled:opacity-50"
        />
        <button
          onClick={() => askStock()}
          disabled={loading || !question.trim()}
          className="bg-primary text-on-primary px-4 py-2.5 rounded-xl font-semibold text-label-md hover:opacity-90 transition-opacity disabled:opacity-40 flex items-center gap-1"
        >
          <span className="material-symbols-outlined text-[18px]">send</span>
        </button>
      </div>

      {/* Loading state with Lottie */}
      {loading && (
        <div className="flex flex-col items-center gap-2 py-4">
          <LottieAnimation src="/animations/ai-thinking.json" className="w-20 h-8" loop />
          <p className="text-body-md text-on-surface-variant">Yanıt hazırlanıyor...</p>
        </div>
      )}

      {error && (
        <div className="bg-error-container rounded-xl p-4">
          <p className="text-body-md text-on-error-container">{error}</p>
        </div>
      )}

      {response && !loading && (
        <div className="bg-surface-container-low rounded-xl p-5 flex flex-col gap-4">
          {response.summary && (
            <p className="text-body-md text-on-surface leading-relaxed">{response.summary}</p>
          )}
          {Array.isArray(response.possibleFactors) && response.possibleFactors.length > 0 && (
            <ul className="flex flex-col gap-2">
              {response.possibleFactors.map((f, i) => (
                <li key={i} className="flex items-start gap-2 text-body-md text-on-surface-variant">
                  <span className="material-symbols-outlined text-primary text-[16px] mt-0.5 flex-shrink-0">check_circle</span>
                  {f}
                </li>
              ))}
            </ul>
          )}
          {response.learningNote && (
            <div className="learning-note">
              <span className="material-symbols-outlined text-tertiary-container text-[20px] mt-0.5 flex-shrink-0" style={{ fontVariationSettings: "'FILL' 1" }}>
                lightbulb
              </span>
              <p className="text-body-md text-on-surface-variant">{response.learningNote}</p>
            </div>
          )}
          {response.disclaimer && (
            <p className="text-xs text-outline italic">{response.disclaimer}</p>
          )}
        </div>
      )}
    </div>
  )
}
