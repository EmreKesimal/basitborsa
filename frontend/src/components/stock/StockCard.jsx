import { useNavigate } from 'react-router-dom'
import { formatCurrency, formatPercent, changeColorClass } from '../../utils/formatters.js'
import { HERO_SYMBOL } from '../../services/stockService.js'
import DataSourceBadge from '../common/DataSourceBadge.jsx'

export default function StockCard({ stock }) {
  const navigate = useNavigate()
  const isHero = stock.symbol === HERO_SYMBOL
  const hasPrice = stock.currentPrice != null
  const hasChange = stock.dailyChangePercent != null
  const isUp = hasChange ? Number(stock.dailyChangePercent) >= 0 : true
  const priceUnavailable = !hasPrice
  const badgeSource = isHero
    ? (priceUnavailable ? 'UNAVAILABLE' : stock.dataSource)
    : 'DEMO_LIMITED'

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
          <DataSourceBadge source={badgeSource} />
        </div>

        {/* Sector */}
        <span className="text-xs text-outline bg-surface-container-low px-2.5 py-1 rounded-full w-fit">
          {stock.sector}
        </span>

        {/* Description */}
        {isHero ? (
          stock.description && (
            <p className="text-body-md text-on-surface-variant line-clamp-2 flex-1">
              {stock.description}
            </p>
          )
        ) : (
          <p className="text-body-md text-amber-800 bg-amber-50 border border-amber-200 rounded-lg p-2.5 leading-snug flex-1">
            Bu hackathon demosunda gerçek veri ve grafik hikâyesi THYAO üzerinde gösterilmektedir.
          </p>
        )}

        {/* Price row */}
        <div className="flex items-end justify-between pt-2 border-t border-surface-container-high">
          <div>
            <p className="text-xs text-outline mb-0.5">Son Fiyat</p>
            {!isHero ? (
              <p className="text-body-md text-amber-700 font-medium">Demo kapsamı</p>
            ) : priceUnavailable ? (
              <p className="text-body-md text-orange-700 font-medium">Gerçek veri yok</p>
            ) : (
              <p className="text-headline-md font-bold text-on-surface">
                {formatCurrency(stock.currentPrice)}
              </p>
            )}
          </div>
          {isHero && hasChange ? (
            <div className="text-right">
              <div className={`flex items-center gap-1 text-label-md ${changeColorClass(stock.dailyChangePercent)}`}>
                <span className="material-symbols-outlined text-[14px]">
                  {isUp ? 'trending_up' : 'trending_down'}
                </span>
                {formatPercent(stock.dailyChangePercent)}
              </div>
              <p className="text-xs text-outline mt-0.5">Bugün</p>
            </div>
          ) : (
            <p className="text-xs text-outline">—</p>
          )}
        </div>

        {/* CTA */}
        <button
          className="w-full mt-1 py-2 rounded-lg border border-primary text-primary text-label-md font-semibold hover:bg-primary hover:text-on-primary transition-colors flex items-center justify-center gap-1"
          onClick={(e) => { e.stopPropagation(); navigate(`/stocks/${stock.symbol}`) }}
        >
          {isHero ? 'Hisseyi İncele' : 'Demo Kartını Aç'}
          <span className="material-symbols-outlined text-[16px]">arrow_forward</span>
        </button>
      </div>
    </div>
  )
}
