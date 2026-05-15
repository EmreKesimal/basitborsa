import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAsync } from '../hooks/useAsync.js'
import { stockService } from '../services/stockService.js'
import { aiService } from '../services/aiService.js'
import StockChart from '../components/stock/StockChart.jsx'
import StoryPanel from '../components/stock/StoryPanel.jsx'
import MetricCard from '../components/stock/MetricCard.jsx'
import AiTeacherBox from '../components/ai/AiTeacherBox.jsx'
import LearningNote from '../components/learning/LearningNote.jsx'
import LoadingState from '../components/common/LoadingState.jsx'
import ErrorState from '../components/common/ErrorState.jsx'
import { formatCurrency, formatPercent, changeColorClass } from '../utils/formatters.js'

export default function StockDetail() {
  const { symbol } = useParams()
  const navigate = useNavigate()
  const [range, setRange] = useState('30d')
  const [selectedEvent, setSelectedEvent] = useState(null)
  const [aiStory, setAiStory] = useState(null)
  const [aiStoryLoading, setAiStoryLoading] = useState(false)

  const { data: stock, loading: stockLoading, error: stockError } = useAsync(
    () => stockService.getOne(symbol), [symbol]
  )
  const { data: priceHistory, loading: pricesLoading } = useAsync(
    () => stockService.getPrices(symbol, range), [symbol, range]
  )
  const { data: events } = useAsync(() => stockService.getEvents(symbol), [symbol])

  async function handleEventClick(event) {
    setSelectedEvent(event)
    setAiStory(null)
    if (!event) return
    setAiStoryLoading(true)
    try {
      const story = await aiService.chartStory(symbol, event.id)
      setAiStory(story)
    } catch {
      // fallback: StoryPanel shows static event data
    } finally {
      setAiStoryLoading(false)
    }
  }

  if (stockLoading) return <LoadingState message="Hisse bilgileri yükleniyor..." />
  if (stockError) return <ErrorState message={stockError} onRetry={() => navigate('/stocks')} />

  return (
    <div className="flex flex-col gap-stack-gap-md">
      <button
        onClick={() => navigate('/stocks')}
        className="flex items-center gap-unit text-outline hover:text-on-surface-variant transition-colors text-label-md w-fit"
      >
        <span className="material-symbols-outlined text-[18px]">arrow_back</span>
        Tüm Hisseler
      </button>

      <div className="flex flex-col lg:flex-row gap-stack-gap-lg">
        {/* Left column */}
        <div className="flex-1 flex flex-col gap-stack-gap-md">
          {/* Header */}
          <div className="card p-stack-gap-md">
            <div className="flex items-start justify-between mb-stack-gap-sm">
              <div>
                <div className="flex items-center gap-unit mb-unit">
                  <span className="bg-surface-container-low text-primary px-3 py-1 rounded-lg text-label-md">
                    BIST
                  </span>
                  <h1 className="text-display-lg font-bold text-on-surface">{stock?.symbol}</h1>
                </div>
                <h2 className="text-headline-md text-on-surface-variant">{stock?.companyName}</h2>
                <span className="text-xs text-outline bg-surface-container-low px-2 py-0.5 rounded-full mt-1 inline-block">
                  {stock?.sector}
                </span>
              </div>
              <div className="text-right">
                <div className="text-headline-lg font-semibold text-on-surface">
                  {formatCurrency(stock?.currentPrice)}
                </div>
                <div className={`text-label-md flex items-center justify-end gap-1 ${changeColorClass(stock?.dailyChangePercent)}`}>
                  {formatPercent(stock?.dailyChangePercent)} (Bugün)
                </div>
              </div>
            </div>
            <p className="text-body-md text-on-surface-variant">{stock?.description}</p>
            {stock?.dataSource && (
              <p className="text-xs text-outline mt-2 italic">{stock.disclaimer}</p>
            )}
          </div>

          {/* Learning note */}
          <LearningNote title="Bunu neden öğreniyorum?">
            Bir şirketi anlamak, grafiğini okuyabilmekten önce gelir. Bu sayfada hem fiyat geçmişini hem de şirketi etkileyen olayları inceleyebilirsin.
          </LearningNote>

          {/* Chart */}
          {pricesLoading ? (
            <LoadingState message="Fiyat verileri yükleniyor..." />
          ) : (
            <StockChart
              priceHistory={priceHistory}
              events={events}
              onEventClick={handleEventClick}
              selectedRange={range}
              onRangeChange={setRange}
            />
          )}

          {/* Metrics */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-stack-gap-sm">
            <MetricCard label="F/K Oranı" value={stock?.peRatio} hint="Fiyat/Kazanç" />
            <MetricCard label="PD/DD" value={stock?.pbRatio} hint="Piyasa/Defter" />
            <MetricCard
              label="Temettü Verimi"
              value={stock?.dividendYield != null ? `%${Number(stock.dividendYield).toFixed(2)}` : null}
            />
            <MetricCard
              label="Piyasa Değeri"
              value={stock?.marketCapBillions != null ? `${Number(stock.marketCapBillions).toFixed(0)} Mlyr ₺` : null}
            />
          </div>

          {/* AI Teacher */}
          {stock && <AiTeacherBox stock={stock} />}
        </div>

        {/* Right sidebar */}
        <aside className="w-full lg:w-80 flex flex-col gap-stack-gap-md">
          {/* Story Panel */}
          <StoryPanel event={selectedEvent} aiStory={aiStory} aiStoryLoading={aiStoryLoading} />

          {/* Events list */}
          {events?.length > 0 && (
            <div className="card p-stack-gap-md flex flex-col gap-stack-gap-sm">
              <h3 className="text-headline-md font-semibold text-on-surface">Önemli Olaylar</h3>
              <div className="flex flex-col gap-2">
                {events.map((e) => (
                  <button
                    key={e.id}
                    onClick={() => handleEventClick(e)}
                    className={`text-left p-2 rounded-lg transition-colors hover:bg-surface-container-low ${
                      selectedEvent?.id === e.id ? 'bg-surface-container-low' : ''
                    }`}
                  >
                    <div className="flex items-center justify-between gap-2">
                      <span className="text-body-md text-on-surface font-medium">{e.title}</span>
                      <span className={`text-label-md flex-shrink-0 ${
                        e.eventType === 'RISE' ? 'text-green-600' : e.eventType === 'FALL' ? 'text-error' : 'text-tertiary'
                      }`}>
                        {e.priceChangePercent != null
                          ? (Number(e.priceChangePercent) >= 0 ? '+' : '') + Number(e.priceChangePercent).toFixed(1) + '%'
                          : ''}
                      </span>
                    </div>
                    <p className="text-xs text-outline">{e.eventDate}</p>
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Understanding Checklist */}
          <div className="card p-stack-gap-md flex flex-col gap-stack-gap-sm">
            <h3 className="text-headline-md font-semibold text-on-surface">Anlama Kontrolü</h3>
            <p className="text-body-sm text-on-surface-variant">Devam etmeden önce kendine sor:</p>
            <ul className="flex flex-col gap-2">
              {[
                'Bu şirketin ne iş yaptığını biliyor musun?',
                'Grafiğin hikâyesini inceledin mi?',
                'Temel göstergelerin ne anlama geldiğini biliyor musun?',
                'Bu fiyat hareketinin neden olmuş olabileceğini açıklayabiliyor musun?',
              ].map((q) => (
                <li key={q} className="flex items-start gap-2 text-body-sm text-on-surface-variant">
                  <span className="material-symbols-outlined text-outline text-[16px] mt-0.5">check_box_outline_blank</span>
                  {q}
                </li>
              ))}
            </ul>
            <p className="disclaimer">Bu platform yatırım tavsiyesi vermez. Eğitim ve demo amaçlıdır.</p>
          </div>
        </aside>
      </div>
    </div>
  )
}
