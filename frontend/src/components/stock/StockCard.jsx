import { useNavigate } from 'react-router-dom'
import { formatCurrency, formatPercent, changeColorClass } from '../../utils/formatters.js'

export default function StockCard({ stock }) {
  const navigate = useNavigate()
  const isUp = Number(stock.dailyChangePercent) >= 0

  return (
    <div
      className="card card-hover p-stack-gap-md flex items-center justify-between cursor-pointer"
      onClick={() => navigate(`/stocks/${stock.symbol}`)}
    >
      <div className="flex items-center gap-stack-gap-sm">
        <div className="w-12 h-12 rounded-full bg-surface-container flex items-center justify-center text-primary font-bold text-label-md flex-shrink-0">
          {stock.symbol.slice(0, 3)}
        </div>
        <div>
          <h3 className="text-headline-md font-semibold text-on-surface">{stock.symbol}</h3>
          <p className="text-body-md text-on-surface-variant">{stock.companyName}</p>
          <span className="text-xs text-outline bg-surface-container-low px-2 py-0.5 rounded-full">
            {stock.sector}
          </span>
        </div>
      </div>

      <div className="text-right flex-shrink-0">
        <div className="text-headline-md font-semibold text-on-surface">
          {formatCurrency(stock.currentPrice)}
        </div>
        <div className={`text-label-md flex items-center justify-end gap-1 ${changeColorClass(stock.dailyChangePercent)}`}>
          <span className="material-symbols-outlined text-[14px]">
            {isUp ? 'trending_up' : 'trending_down'}
          </span>
          {formatPercent(stock.dailyChangePercent)}
        </div>
        <button className="mt-2 text-xs text-primary font-semibold hover:opacity-80 transition-opacity flex items-center gap-1 ml-auto">
          İncele
          <span className="material-symbols-outlined text-[14px]">arrow_forward</span>
        </button>
      </div>
    </div>
  )
}
