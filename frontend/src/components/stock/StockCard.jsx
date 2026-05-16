import { useNavigate } from 'react-router-dom'
import { formatCurrency, formatPercent, changeColorClass } from '../../utils/formatters.js'
import DataSourceBadge from '../common/DataSourceBadge.jsx'

export default function StockCard({ stock }) {
  const navigate = useNavigate()
  const isUp = Number(stock.dailyChangePercent) >= 0

  return (
    <div
      className="card card-hover cursor-pointer flex flex-col overflow-hidden"
      onClick={() => navigate(`/stocks/${stock.symbol}`)}
    >
      {/* Top accent */}
      <div className={`h-1 w-full ${isUp ? 'bg-positive' : 'bg-error'}`} />

      <div className="p-5 flex flex-col gap-4 flex-1">
        {/* Header row */}
        <div className="flex items-start justify-between gap-2">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-xl bg-surface-container-low flex items-center justify-center flex-shrink-0">
              <span className="text-xs font-bold text-primary leading-none text-center">
                {stock.symbol.slice(0, 4)}
              </span>
            </div>
            <div className="min-w-0">
              <h3 className="text-headline-md font-bold text-on-surface leading-tight truncate">
                {stock.symbol}
              </h3>
              <p className="text-xs text-on-surface-variant truncate">{stock.companyName}</p>
            </div>
          </div>
          <DataSourceBadge source={stock.dataSource} />
        </div>

        {/* Sector */}
        <span className="text-xs text-outline bg-surface-container-low px-2.5 py-1 rounded-full w-fit">
          {stock.sector}
        </span>

        {/* Description */}
        {stock.description && (
          <p className="text-body-md text-on-surface-variant line-clamp-2 flex-1">
            {stock.description}
          </p>
        )}

        {/* Price row */}
        <div className="flex items-end justify-between pt-2 border-t border-surface-container-high">
          <div>
            <p className="text-xs text-outline mb-0.5">Son Fiyat</p>
            <p className="text-headline-md font-bold text-on-surface">
              {formatCurrency(stock.currentPrice)}
            </p>
          </div>
          <div className="text-right">
            <div className={`flex items-center gap-1 text-label-md ${changeColorClass(stock.dailyChangePercent)}`}>
              <span className="material-symbols-outlined text-[14px]">
                {isUp ? 'trending_up' : 'trending_down'}
              </span>
              {formatPercent(stock.dailyChangePercent)}
            </div>
            <p className="text-xs text-outline mt-0.5">Bugün</p>
          </div>
        </div>

        {/* CTA */}
        <button
          className="w-full mt-1 py-2 rounded-lg border border-primary text-primary text-label-md font-semibold hover:bg-primary hover:text-on-primary transition-colors flex items-center justify-center gap-1"
          onClick={(e) => { e.stopPropagation(); navigate(`/stocks/${stock.symbol}`) }}
        >
          Hisseyi İncele
          <span className="material-symbols-outlined text-[16px]">arrow_forward</span>
        </button>
      </div>
    </div>
  )
}
