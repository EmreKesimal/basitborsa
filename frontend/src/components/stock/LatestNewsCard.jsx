import { formatDate } from '../../utils/formatters.js'

const SOURCE_LABELS = {
  KAP: { label: 'KAP', cls: 'text-blue-700 bg-blue-50 border-blue-200' },
  EXTERNAL_NEWS: { label: 'Haber', cls: 'text-emerald-700 bg-emerald-50 border-emerald-200' },
  CACHED_NEWS: { label: 'Cache', cls: 'text-amber-700 bg-amber-50 border-amber-200' },
}

const CATEGORY_LABELS = {
  COMPANY:   { label: 'Şirket',        cls: 'text-emerald-700 bg-emerald-50 border-emerald-200' },
  SECTOR:    { label: 'Sektör',        cls: 'text-sky-700 bg-sky-50 border-sky-200' },
  COMMODITY: { label: 'Petrol/Yakıt',  cls: 'text-orange-700 bg-orange-50 border-orange-200' },
  MACRO:     { label: 'Küresel',       cls: 'text-purple-700 bg-purple-50 border-purple-200' },
  GLOBAL:    { label: 'Küresel',       cls: 'text-purple-700 bg-purple-50 border-purple-200' },
}

function SourceBadge({ sourceType }) {
  const conf = SOURCE_LABELS[sourceType] || SOURCE_LABELS.EXTERNAL_NEWS
  return (
    <span className={`text-[10px] px-2 py-0.5 rounded-full font-medium border ${conf.cls}`}>
      {conf.label}
    </span>
  )
}

function CategoryBadge({ feedCategory }) {
  if (!feedCategory) return null
  const conf = CATEGORY_LABELS[feedCategory]
  if (!conf) return null
  return (
    <span className={`text-[10px] px-2 py-0.5 rounded-full font-medium border ${conf.cls}`}>
      {conf.label}
    </span>
  )
}

export default function LatestNewsCard({ items, title = 'Son İlgili Haberler', emptyHint }) {
  return (
    <div className="card p-5 flex flex-col gap-3">
      <div className="flex items-center gap-2">
        <span
          className="material-symbols-outlined text-primary text-[20px]"
          style={{ fontVariationSettings: "'FILL' 1" }}
        >
          newspaper
        </span>
        <h3 className="text-headline-md font-bold text-on-surface">{title}</h3>
      </div>

      {(!items || items.length === 0) ? (
        <p className="text-body-md text-on-surface-variant">
          {emptyHint || 'Bu hisse için eşleşen güncel haber bulunamadı.'}
        </p>
      ) : (
        <ul className="flex flex-col gap-2">
          {items.map((n) => (
            <li
              key={n.id || (n.sourceUrl || n.title) + n.publishedAt}
              className="bg-surface-container-low rounded-lg p-3 flex flex-col gap-1"
            >
              <div className="flex items-start justify-between gap-2">
                <span className="text-body-md text-on-surface font-medium leading-tight">
                  {n.title}
                </span>
                <div className="flex items-center gap-1 shrink-0">
                  <CategoryBadge feedCategory={n.feedCategory} />
                  <SourceBadge sourceType={n.sourceType} />
                </div>
              </div>
              <div className="text-xs text-outline flex flex-wrap gap-x-2">
                {n.publishedAt && <span>{formatDate(n.publishedAt)}</span>}
                {n.sourceName && <span>· {n.sourceName}</span>}
                {n.sourceUrl && (
                  <a
                    href={n.sourceUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-primary hover:underline ml-auto"
                  >
                    Kaynağa git ↗
                  </a>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}

      <p className="text-xs text-outline italic border-t border-surface-container-high pt-3">
        Haberler eğitim amaçlıdır. Yatırım tavsiyesi değildir.
      </p>
    </div>
  )
}
