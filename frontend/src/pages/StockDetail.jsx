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
import DataSourceBadge from '../components/common/DataSourceBadge.jsx'
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

  const isUp = Number(stock?.dailyChangePercent) >= 0

  return (
    <div className="flex flex-col gap-6">
      {/* Breadcrumb */}
      <button
        onClick={() => navigate('/stocks')}
        className="flex items-center gap-1.5 text-on-surface-variant hover:text-primary transition-colors text-label-md w-fit"
      >
        <span className="material-symbols-outlined text-[16px]">arrow_back</span>
        Tüm Hisseler
      </button>

      {/* ── Stock identity card ─────────────────────────── */}
      <div className="card p-5 sm:p-6">
        <div className="flex flex-wrap items-start justify-between gap-4">
          {/* Left: identity */}
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 rounded-2xl bg-surface-container-low border border-surface-container-high flex items-center justify-center flex-shrink-0">
              <span className="text-sm font-black text-primary leading-none text-center">
                {stock?.symbol?.slice(0, 4)}
              </span>
            </div>
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <h1 className="text-headline-lg font-black text-primary">{stock?.symbol}</h1>
                <span className="text-xs bg-surface-container text-outline px-2 py-0.5 rounded-full">BIST</span>
                {stock?.dataSource && <DataSourceBadge source={stock.dataSource} />}
              </div>
              <h2 className="text-body-lg text-on-surface-variant font-medium">{stock?.companyName}</h2>
              <span className="text-xs text-outline bg-surface-container-low px-2.5 py-0.5 rounded-full inline-block mt-1">
                {stock?.sector}
              </span>
            </div>
          </div>

          {/* Right: price */}
          <div className="text-right">
            <div className="text-headline-lg font-black text-on-surface">
              {formatCurrency(stock?.currentPrice)}
            </div>
            <div className={`flex items-center justify-end gap-1 text-label-md mt-0.5 ${changeColorClass(stock?.dailyChangePercent)}`}>
              <span className="material-symbols-outlined text-[14px]">
                {isUp ? 'trending_up' : 'trending_down'}
              </span>
              {formatPercent(stock?.dailyChangePercent)}
              <span className="text-outline font-normal ml-1">Bugün</span>
            </div>
          </div>
        </div>

        {stock?.disclaimer && (
          <p className="text-xs text-outline mt-4 pt-4 border-t border-surface-container-high italic">
            {stock.disclaimer}
          </p>
        )}
      </div>

      {/* ── Main content ──────────────────────────────────── */}
      <div className="flex flex-col xl:flex-row gap-6">

        {/* ── LEFT column: chart + company + metrics + AI ── */}
        <div className="flex-1 min-w-0 flex flex-col gap-6">

          {/* Chart HERO */}
          {pricesLoading ? (
            <LoadingState message="Fiyat verileri yükleniyor..." />
          ) : (
            <StockChart
              priceHistory={priceHistory}
              events={events}
              onEventClick={handleEventClick}
              selectedRange={range}
              onRangeChange={setRange}
              selectedEvent={selectedEvent}
            />
          )}

          {/* Learning note */}
          <LearningNote title="Bunu neden öğreniyorum?">
            Bir şirketi anlamak, grafiğini okuyabilmekten önce gelir. Fiyat geçmişini ve
            olayları incelerken ne olmuş olabileceğini AI destekli analizlerle öğren.
          </LearningNote>

          {/* Company info */}
          {stock?.description && (
            <div className="card p-5 sm:p-6 flex flex-col gap-3">
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-primary text-[20px]" style={{ fontVariationSettings: "'FILL' 1" }}>
                  corporate_fare
                </span>
                <h3 className="text-headline-md font-bold text-on-surface">Bu şirket ne yapıyor?</h3>
              </div>
              <p className="text-body-md text-on-surface-variant leading-relaxed">{stock.description}</p>
            </div>
          )}

          {/* Metrics */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <MetricCard label="F/K Oranı" value={stock?.peRatio} hint="Fiyat / Kazanç" />
            <MetricCard label="PD/DD" value={stock?.pbRatio} hint="Piyasa / Defter" />
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

        {/* ── RIGHT sidebar: story + events + checklist ─── */}
        <aside className="w-full xl:w-[380px] flex-shrink-0 flex flex-col gap-6">

          {/* HERO: Grafiğin Hikâyesi */}
          <StoryPanel
            event={selectedEvent}
            aiStory={aiStory}
            aiStoryLoading={aiStoryLoading}
          />

          {/* Events list */}
          {events?.length > 0 && (
            <div className="card p-5 flex flex-col gap-4">
              <h3 className="text-headline-md font-bold text-on-surface">Önemli Olaylar</h3>
              <div className="flex flex-col gap-1.5">
                {events.map((e) => {
                  const isSelected = selectedEvent?.id === e.id
                  const changeColor =
                    e.eventType === 'RISE' ? 'text-positive' :
                    e.eventType === 'FALL' ? 'text-error' : 'text-tertiary'
                  return (
                    <button
                      key={e.id}
                      onClick={() => handleEventClick(e)}
                      className={`text-left px-3 py-2.5 rounded-xl transition-all ${
                        isSelected
                          ? 'bg-surface-container-low border border-primary'
                          : 'hover:bg-surface-container-low border border-transparent'
                      }`}
                    >
                      <div className="flex items-center justify-between gap-2">
                        <span className="text-body-md text-on-surface font-medium leading-tight">
                          {e.title}
                        </span>
                        <span className={`text-label-md flex-shrink-0 font-bold ${changeColor}`}>
                          {e.priceChangePercent != null
                            ? (Number(e.priceChangePercent) >= 0 ? '+' : '') +
                              Number(e.priceChangePercent).toFixed(1) + '%'
                            : ''}
                        </span>
                      </div>
                      <p className="text-xs text-outline mt-0.5">{e.eventDate}</p>
                    </button>
                  )
                })}
              </div>
            </div>
          )}

          {/* Understanding checklist */}
          <div className="card p-5 flex flex-col gap-4">
            <div className="flex items-center gap-2">
              <span className="material-symbols-outlined text-primary text-[20px]" style={{ fontVariationSettings: "'FILL' 1" }}>
                checklist
              </span>
              <h3 className="text-headline-md font-bold text-on-surface">Anlama Kontrolü</h3>
            </div>
            <p className="text-body-md text-on-surface-variant">Devam etmeden önce kendine sor:</p>
            <ul className="flex flex-col gap-2.5">
              {[
                'Bu şirketin ne iş yaptığını biliyor musun?',
                'Grafiğin hikâyesini inceledin mi?',
                'Temel göstergelerin ne anlama geldiğini biliyor musun?',
                'Fiyat hareketinin neden olmuş olabileceğini açıklayabiliyor musun?',
              ].map((q) => (
                <li key={q} className="flex items-start gap-2.5 text-body-md text-on-surface-variant">
                  <span className="material-symbols-outlined text-outline text-[18px] mt-0.5 flex-shrink-0">
                    check_box_outline_blank
                  </span>
                  {q}
                </li>
              ))}
            </ul>
            <p className="text-xs text-outline italic border-t border-surface-container-high pt-3">
              Bu platform yatırım tavsiyesi vermez. Eğitim ve demo amaçlıdır.
            </p>
          </div>
        </aside>
      </div>

      {/* Global disclaimer */}
      <p className="text-xs text-outline text-center pb-2">
        Veriler gecikmeli/gün sonu olabilir. Bu platform yatırım tavsiyesi vermez.
      </p>
    </div>
  )
}
