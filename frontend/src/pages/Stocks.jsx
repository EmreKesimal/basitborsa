import { useState } from 'react'
import { useAsync } from '../hooks/useAsync.js'
import { stockService } from '../services/stockService.js'
import StockCard from '../components/stock/StockCard.jsx'
import LoadingState from '../components/common/LoadingState.jsx'
import ErrorState from '../components/common/ErrorState.jsx'

export default function Stocks() {
  const [query, setQuery] = useState('')
  const { data: stocks, loading, error, refetch } = useAsync(() => stockService.getAll(), [])

  const filtered = stocks?.filter(
    (s) =>
      s.symbol.toLowerCase().includes(query.toLowerCase()) ||
      s.companyName.toLowerCase().includes(query.toLowerCase())
  )

  return (
    <div className="flex flex-col gap-stack-gap-md">
      <div>
        <h1 className="text-headline-lg font-semibold text-on-surface">Hisseler</h1>
        <p className="text-body-md text-on-surface-variant mt-1">
          Seçili BIST hisselerini inceleyin. Gecikmeli/gün sonu verileri kullanılmaktadır.
        </p>
      </div>

      {/* Search */}
      <div className="relative">
        <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline">search</span>
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Hisse veya şirket adıyla ara..."
          className="w-full h-12 pl-12 pr-4 bg-surface-container-low border-none rounded-xl text-body-md text-on-surface placeholder:text-outline focus:ring-2 focus:ring-primary outline-none"
        />
      </div>

      {loading && <LoadingState message="Hisseler yükleniyor..." />}
      {error && <ErrorState message={error} onRetry={refetch} />}

      {!loading && !error && (
        <div className="flex flex-col gap-stack-gap-sm">
          {filtered?.length === 0 ? (
            <div className="text-center py-12 text-on-surface-variant">
              <span className="material-symbols-outlined text-4xl">search_off</span>
              <p className="mt-2 text-body-md">"{query}" için sonuç bulunamadı.</p>
            </div>
          ) : (
            filtered?.map((stock) => <StockCard key={stock.symbol} stock={stock} />)
          )}
        </div>
      )}

      <p className="text-xs text-outline text-center">
        Veriler gecikmeli/gün sonu olabilir. Bu platform yatırım tavsiyesi vermez.
      </p>
    </div>
  )
}
