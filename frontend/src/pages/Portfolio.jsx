import { useState, useCallback } from 'react'
import { useAsync } from '../hooks/useAsync.js'
import { portfolioService } from '../services/portfolioService.js'
import PortfolioCard from '../components/portfolio/PortfolioCard.jsx'
import LoadingState from '../components/common/LoadingState.jsx'
import ErrorState from '../components/common/ErrorState.jsx'
import LearningNote from '../components/learning/LearningNote.jsx'
import { formatCurrency, formatPercent, changeColorClass } from '../utils/formatters.js'

export default function Portfolio() {
  const [removeError, setRemoveError] = useState(null)

  const fetchPortfolio = useCallback(() => portfolioService.get(), [])
  const { data: portfolio, loading, error, refetch } = useAsync(fetchPortfolio, [])

  async function handleRemove(itemId) {
    setRemoveError(null)
    try {
      await portfolioService.removeItem(itemId)
      refetch()
    } catch (err) {
      setRemoveError(err.message)
    }
  }

  if (loading) return <LoadingState message="Portföy yükleniyor..." />
  if (error) return <ErrorState message={error} onRetry={refetch} />

  const hasItems = portfolio?.items?.length > 0

  return (
    <div className="flex flex-col gap-stack-gap-lg">
      {/* Balance hero */}
      <section className="card text-center flex flex-col items-center gap-unit py-stack-gap-lg">
        <h2 className="text-body-lg text-on-surface-variant">Sanal Bakiye</h2>
        <div className="text-display-lg font-bold text-primary">
          {formatCurrency(portfolio?.virtualBalance, 2)}
        </div>
        <p className="text-body-md text-outline">Toplam Varlık Değeri: {formatCurrency(portfolio?.totalValue)}</p>
        {hasItems && (
          <div className={`text-label-md font-semibold flex items-center gap-1 ${changeColorClass(portfolio?.totalGainLoss)}`}>
            <span className="material-symbols-outlined text-[16px]">
              {Number(portfolio?.totalGainLoss) >= 0 ? 'trending_up' : 'trending_down'}
            </span>
            Hisse Kar/Zarar: {formatCurrency(portfolio?.totalGainLoss)} ({formatPercent(portfolio?.totalGainLossPercent)})
          </div>
        )}
      </section>

      {/* Learning note */}
      {hasItems && (
        <LearningNote title="Portföy Analizi">
          Portföyünüzdeki her hisseyi neden seçtiğinizi hatırlıyor musunuz? Bir sonraki işlemden önce her hissenin risklerini ve temel göstergelerini tekrar gözden geçirmenizi öneririz.
        </LearningNote>
      )}

      {removeError && (
        <div className="p-3 rounded-xl text-body-md bg-error-container text-on-error-container text-center">
          {removeError}
        </div>
      )}

      {/* Holdings */}
      <section className="flex flex-col gap-stack-gap-md">
        <h3 className="text-headline-lg font-semibold text-on-surface border-b border-surface-container-high pb-unit">
          Sahip Olduğum Hisseler
        </h3>

        {hasItems ? (
          <div className="flex flex-col gap-stack-gap-sm">
            {portfolio.items.map((item) => (
              <PortfolioCard key={item.id} item={item} onRemove={handleRemove} />
            ))}
          </div>
        ) : (
          <div className="text-center py-16 flex flex-col items-center gap-4 text-on-surface-variant">
            <span className="material-symbols-outlined text-5xl text-outline">account_balance_wallet</span>
            <div>
              <p className="text-body-lg font-semibold">Henüz hisse eklemediniz.</p>
              <p className="text-body-md text-outline mt-1">
                Hisseler sayfasından bir şirket seçin ve sanal portföyünüze ekleyin.
              </p>
            </div>
          </div>
        )}
      </section>

      <p className="disclaimer">
        Veriler gecikmeli/gün sonu olabilir. Bu platform yatırım tavsiyesi vermez.
        Bu sanal portföy eğitim amaçlıdır; gerçek para içermez.
      </p>
    </div>
  )
}
