import { useState } from 'react'
import { useAsync } from '../hooks/useAsync.js'
import { stockService } from '../services/stockService.js'
import StockCard from '../components/stock/StockCard.jsx'
import LoadingState from '../components/common/LoadingState.jsx'
import ErrorState from '../components/common/ErrorState.jsx'
import EmptyState from '../components/common/EmptyState.jsx'

export default function Stocks() {
  const [query, setQuery] = useState('')
  const { data: stocks, loading, error, refetch } = useAsync(() => stockService.getAll(), [])

  const filtered = stocks?.filter(
    (s) =>
      s.symbol.toLowerCase().includes(query.toLowerCase()) ||
      s.companyName.toLowerCase().includes(query.toLowerCase())
  )

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
      {error && <ErrorState message={error} onRetry={refetch} />}

      {/* Grid */}
      {!loading && !error && (
        <>
          {filtered?.length === 0 ? (
            <EmptyState
              icon="search_off"
              message={query ? `"${query}" için sonuç bulunamadı.` : 'Hisse bulunamadı.'}
            />
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
              {filtered?.map((stock) => (
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
