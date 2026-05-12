import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAsync } from '../hooks/useAsync.js'
import { stockService } from '../services/stockService.js'
import { portfolioService } from '../services/portfolioService.js'
import StockChart from '../components/stock/StockChart.jsx'
import StoryPanel from '../components/stock/StoryPanel.jsx'
import MetricCard from '../components/stock/MetricCard.jsx'
import AiTeacherBox from '../components/ai/AiTeacherBox.jsx'
import ChecklistModal from '../components/portfolio/ChecklistModal.jsx'
import LearningNote from '../components/learning/LearningNote.jsx'
import LoadingState from '../components/common/LoadingState.jsx'
import ErrorState from '../components/common/ErrorState.jsx'
import { formatCurrency, formatPercent, changeColorClass } from '../utils/formatters.js'

export default function StockDetail() {
  const { symbol } = useParams()
  const navigate = useNavigate()
  const [range, setRange] = useState('30d')
  const [selectedEvent, setSelectedEvent] = useState(null)
  const [showChecklist, setShowChecklist] = useState(false)
  const [addMessage, setAddMessage] = useState(null)

  const { data: stock, loading: stockLoading, error: stockError } = useAsync(
    () => stockService.getOne(symbol), [symbol]
  )
  const { data: priceHistory, loading: pricesLoading, refetch: refetchPrices } = useAsync(
    () => stockService.getPrices(symbol, range), [symbol, range]
  )
  const { data: events } = useAsync(() => stockService.getEvents(symbol), [symbol])

  async function handleAddToPortfolio(quantity) {
    try {
      await portfolioService.addItem(symbol, quantity)
      setAddMessage({ type: 'success', text: `${quantity} adet ${symbol} sanal portföyüne eklendi!` })
    } catch (err) {
      setAddMessage({ type: 'error', text: err.message })
    } finally {
      setShowChecklist(false)
      setTimeout(() => setAddMessage(null), 4000)
    }
  }

  if (stockLoading) return <LoadingState message="Hisse bilgileri yükleniyor..." />
  if (stockError) return <ErrorState message={stockError} onRetry={() => navigate('/stocks')} />

  return (
    <div className="flex flex-col gap-stack-gap-md">
      {/* Back */}
      <button
        onClick={() => navigate('/stocks')}
        className="flex items-center gap-unit text-outline hover:text-on-surface-variant transition-colors text-label-md w-fit"
      >
        <span className="material-symbols-outlined text-[18px]">arrow_back</span>
        Tüm Hisseler
      </button>

      {/* Add success/error message */}
      {addMessage && (
        <div className={`p-3 rounded-xl text-body-md font-semibold text-center ${
          addMessage.type === 'success'
            ? 'bg-green-50 text-green-700 border border-green-200'
            : 'bg-error-container text-on-error-container border border-error'
        }`}>
          {addMessage.text}
        </div>
      )}

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
              onEventClick={setSelectedEvent}
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
          <StoryPanel event={selectedEvent} />

          {/* Events list */}
          {events?.length > 0 && (
            <div className="card p-stack-gap-md flex flex-col gap-stack-gap-sm">
              <h3 className="text-headline-md font-semibold text-on-surface">Önemli Olaylar</h3>
              <div className="flex flex-col gap-2">
                {events.map((e) => (
                  <button
                    key={e.id}
                    onClick={() => setSelectedEvent(e)}
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

          {/* Add to portfolio */}
          <div className="card p-stack-gap-md flex flex-col gap-stack-gap-sm">
            <h3 className="text-headline-md font-semibold text-on-surface">Sanal Portföy</h3>
            <p className="text-body-md text-on-surface-variant">
              Bu hisseyi sanal portföyüne eklemeden önce kontrol listesini gözden geçir.
            </p>
            <button
              onClick={() => setShowChecklist(true)}
              className="btn-primary w-full"
            >
              <span className="material-symbols-outlined text-[18px]">add_circle</span>
              Portföye Ekle
            </button>
            <p className="disclaimer">Bu işlem sadece sanal portföy içindir. Gerçek para içermez.</p>
          </div>
        </aside>
      </div>

      {showChecklist && (
        <ChecklistModal
          stock={stock}
          onConfirm={handleAddToPortfolio}
          onClose={() => setShowChecklist(false)}
        />
      )}
    </div>
  )
}
