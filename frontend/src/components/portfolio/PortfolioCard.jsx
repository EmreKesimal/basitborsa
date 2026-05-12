import { formatCurrency, formatPercent, changeColorClass } from '../../utils/formatters.js'

export default function PortfolioCard({ item, onRemove }) {
  return (
    <div className="card p-stack-gap-md flex items-center justify-between gap-4">
      <div className="flex items-center gap-stack-gap-sm min-w-0">
        <div className="w-12 h-12 rounded-full bg-surface-container flex items-center justify-center text-primary font-bold text-label-md flex-shrink-0">
          {item.symbol.slice(0, 3)}
        </div>
        <div className="min-w-0">
          <h4 className="text-headline-md font-semibold text-on-surface">{item.symbol}</h4>
          <p className="text-body-md text-on-surface-variant truncate">{item.companyName}</p>
          <p className="text-xs text-outline">
            {item.quantity} adet · Maliyet: {formatCurrency(item.averagePrice)}
          </p>
        </div>
      </div>

      <div className="text-right flex-shrink-0">
        <div className="text-headline-md font-semibold text-on-surface">{formatCurrency(item.currentValue)}</div>
        <div className={`text-label-md flex items-center justify-end gap-1 ${changeColorClass(item.gainLoss)}`}>
          <span className="material-symbols-outlined text-[14px]">
            {Number(item.gainLoss) >= 0 ? 'trending_up' : 'trending_down'}
          </span>
          {formatCurrency(item.gainLoss)} ({formatPercent(item.gainLossPercent)})
        </div>
        <button
          onClick={() => onRemove(item.id)}
          className="mt-2 text-xs text-error hover:opacity-70 transition-opacity flex items-center gap-1 ml-auto"
        >
          <span className="material-symbols-outlined text-[13px]">delete</span>
          Çıkar
        </button>
      </div>
    </div>
  )
}
