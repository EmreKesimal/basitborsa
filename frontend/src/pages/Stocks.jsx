import { useState, useEffect, useCallback } from 'react'
import { stockService } from '../services/stockService.js'
import StockCard from '../components/stock/StockCard.jsx'
import LoadingState from '../components/common/LoadingState.jsx'
import ErrorState from '../components/common/ErrorState.jsx'
import EmptyState from '../components/common/EmptyState.jsx'

export default function Stocks() {
  const [query, setQuery] = useState('')
  const [stocks, setStocks] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await stockService.getStocks()
      setStocks(Array.isArray(data) ? data : [])
    } catch (err) {
      setError(err?.message || 'Hisseler yüklenemedi.')
      setStocks([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const safeStocks = Array.isArray(stocks) ? stocks : []
  const q = query.trim().toLowerCase()
  const filtered = safeStocks.filter((s) => {
    if (!s) return false
    if (!q) return true
    const symbol = (s.symbol || '').toLowerCase()
    const name = (s.companyName || '').toLowerCase()
    return symbol.includes(q) || name.includes(q)
  })

  return (
    <div className="flex flex-col gap-8">
      {/* Header */}
      <div className="flex flex-col gap-1">
        <h1 className="text-headline-lg font-bold text-on-surface">Hisseler</h1>
        <p className="text-body-md text-on-surface-variant">
          Seçili BIST hisselerini inceleyin — gecikmeli/gün sonu verileri kullanılmaktadır.
          Bu hackathon demosunda gerçek veri ve grafik hikâyesi yalnızca THYAO için aktiftir.
        </p>
      </div>

      {/* Search */}
      <div className="relative max-w-xl">
        <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline text-[18px]">
          search
        </span>
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Hisse kodu veya şirket adıyla ara..."
          className="w-full h-12 pl-11 pr-4 bg-surface-container-lowest border border-surface-container-high rounded-xl text-body-md text-on-surface placeholder:text-outline focus:ring-2 focus:ring-primary focus:border-transparent outline-none shadow-card"
        />
        {query && (
          <button
            onClick={() => setQuery('')}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-outline hover:text-on-surface transition-colors"
          >
            <span className="material-symbols-outlined text-[18px]">close</span>
          </button>
        )}
      </div>

      {/* States */}
      {loading && (
        <LoadingState
          message="Hisseler yükleniyor..."
          lottie="/animations/finance-chart.json"
        />
      )}
      {!loading && error && <ErrorState message={error} onRetry={load} />}

      {/* Grid */}
      {!loading && !error && (
        <>
          {safeStocks.length === 0 ? (
            <EmptyState
              icon="inbox"
              message="Şu anda gösterilecek hisse yok."
            />
          ) : filtered.length === 0 ? (
            <EmptyState
              icon="search_off"
              message={query ? `"${query}" için sonuç bulunamadı.` : 'Hisse bulunamadı.'}
            />
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
              {filtered.map((stock) => (
                <StockCard key={stock.symbol} stock={stock} />
              ))}
            </div>
          )}
        </>
      )}

      <p className="text-xs text-outline text-center">
        Veriler gecikmeli/gün sonu olabilir. Bu platform yatırım tavsiyesi vermez.
      </p>
    </div>
  )
}
