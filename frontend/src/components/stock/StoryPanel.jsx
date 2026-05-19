import { formatDate, formatPercent } from '../../utils/formatters.js'
import DataSourceBadge from '../common/DataSourceBadge.jsx'

const SOURCE_BADGE = {
  ai_generated: { label: 'Yapay Zekâ',     cls: 'text-green-700 bg-green-50 border-green-200' },
  fallback:     { label: 'Yedek İçerik',   cls: 'text-orange-700 bg-orange-50 border-orange-200' },
  UNAVAILABLE:  { label: 'Veri Yok',       cls: 'text-orange-700 bg-orange-50 border-orange-200' },
}

function SectionBlock({ title, content }) {
  if (!content) return null
  return (
    <div className="flex flex-col gap-1">
      <h5 className="text-label-md text-primary font-semibold">{title}</h5>
      <p className="text-body-md text-on-surface-variant leading-relaxed">{content}</p>
    </div>
  )
}

function RelevantNewsList({ items }) {
  const safeItems = Array.isArray(items) ? items : []
  if (safeItems.length === 0) return null
  return (
    <div className="flex flex-col gap-2">
      <h5 className="text-label-md text-primary font-semibold">Kullanılan Gerçek Haberler</h5>
      <ul className="flex flex-col gap-2">
        {safeItems.map((n, i) => (
          <li key={(n.url || n.title) + i} className="bg-surface-container-low rounded-lg p-3 flex flex-col gap-1">
            <div className="flex items-start justify-between gap-2">
              <span className="text-body-md text-on-surface font-medium leading-tight">{n.title}</span>
              {n.dataSource && <DataSourceBadge source={n.dataSource} />}
            </div>
            <div className="text-xs text-outline flex flex-wrap gap-x-2">
              {n.publishedAt && <span>{formatDate(n.publishedAt)}</span>}
              {n.sourceName && <span>· {n.sourceName}</span>}
              {n.url && (
                <a
                  href={n.url}
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
    </div>
  )
}

export default function StoryPanel({ event, clickedDate, aiStory, aiStoryLoading }) {
  if (!event && !clickedDate && !aiStory && !aiStoryLoading) {
    return (
      <div className="card overflow-hidden">
        <div className="h-1 w-full bg-gradient-to-r from-primary to-tertiary-container" />
        <div className="p-6 flex flex-col items-center text-center gap-4 py-14">
          <div className="w-16 h-16 rounded-2xl bg-surface-container-low flex items-center justify-center">
            <span className="material-symbols-outlined text-primary text-[36px]" style={{ fontVariationSettings: "'FILL' 1" }}>
              auto_stories
            </span>
          </div>
          <div>
            <h3 className="text-headline-md font-bold text-on-surface mb-2">Grafiğin Hikâyesi</h3>
            <p className="text-body-md text-on-surface-variant max-w-xs">
              Grafikteki bir noktaya tıkla, o döneme ait gerçek haberlerle birlikte yapay zekâ açıklamasını gör.
            </p>
          </div>
          <div className="flex items-center gap-2 text-xs text-outline bg-surface-container-low rounded-lg px-3 py-2">
            <span className="material-symbols-outlined text-[14px]">touch_app</span>
            Grafikte herhangi bir tarihe tıkla
          </div>
        </div>
      </div>
    )
  }

  const isRise = event?.eventType === 'RISE'
  const typeColor = isRise ? 'text-positive' : event?.eventType === 'FALL' ? 'text-error' : 'text-tertiary'
  const typeLabel = isRise ? 'Yükseliş' : event?.eventType === 'FALL' ? 'Düşüş' : 'Tarafsız'
  const badge = aiStory ? SOURCE_BADGE[aiStory.sourceType] : null
  const isUnavailable = aiStory?.sourceType === 'UNAVAILABLE'

  return (
    <div className="card overflow-hidden">
      <div className="h-1 w-full bg-gradient-to-r from-primary to-tertiary-container" />
      <div className="p-6 flex flex-col gap-5">

        <div className="flex items-center gap-2">
          <span className="material-symbols-outlined text-primary text-[20px]" style={{ fontVariationSettings: "'FILL' 1" }}>
            auto_stories
          </span>
          <h3 className="text-headline-md font-bold text-on-surface">Grafiğin Hikâyesi</h3>
        </div>

        {event ? (
          <div className="bg-surface-container-low rounded-xl p-4 flex items-start justify-between gap-3">
            <div className="min-w-0">
              <h4 className="text-body-md font-bold text-on-surface">{event.title}</h4>
              <p className="text-xs text-outline mt-0.5">
                {formatDate(event.eventDate)}
                {clickedDate && clickedDate !== event.eventDate && (
                  <> · tıklanan tarih: {formatDate(clickedDate)}</>
                )}
              </p>
              <p className="text-xs text-outline italic mt-1">Eğitsel öğrenme notu — gerçek haber değildir.</p>
            </div>
            <div className="text-right flex-shrink-0">
              <span className={`text-label-md font-bold ${typeColor}`}>
                {formatPercent(event.priceChangePercent)}
              </span>
              <p className={`text-xs mt-0.5 ${typeColor}`}>{typeLabel}</p>
            </div>
          </div>
        ) : clickedDate ? (
          <div className="bg-surface-container-low rounded-xl p-4">
            <h4 className="text-body-md font-bold text-on-surface">Seçilen tarih</h4>
            <p className="text-xs text-outline mt-0.5">{formatDate(clickedDate)}</p>
          </div>
        ) : null}

        <div className="w-full h-px bg-outline-variant opacity-30" />

        {aiStoryLoading && (
          <div className="flex flex-col items-center gap-3 py-6">
            <div className="w-5 h-5 border-2 border-primary border-t-transparent rounded-full animate-spin" />
            <p className="text-body-md text-on-surface-variant">Yapay zekâ analiz ediyor...</p>
          </div>
        )}

        {aiStory && !aiStoryLoading && (
          <div className="flex flex-col gap-5">
            <div className="flex flex-wrap items-center gap-2">
              {badge && (
                <span className={`text-xs px-2.5 py-1 rounded-full font-medium border w-fit ${badge.cls}`}>
                  {badge.label}
                </span>
              )}
              {Array.isArray(aiStory.sourcesUsed) && aiStory.sourcesUsed.map((s) => (
                <DataSourceBadge key={s} source={s} />
              ))}
            </div>

            {isUnavailable && (
              <div className="bg-orange-50 border border-orange-200 rounded-lg p-3 text-sm text-orange-900">
                Gerçek piyasa verisi henüz çekilmedi. Sayfayı birkaç saniye sonra yenileyin.
              </div>
            )}

            {aiStory.summary && (
              <p className="text-body-md text-on-surface font-medium leading-relaxed">
                {aiStory.summary}
              </p>
            )}

            {Array.isArray(aiStory.sections) && aiStory.sections.map((s, i) => (
              <SectionBlock key={i} title={s.title} content={s.content} />
            ))}

            <RelevantNewsList items={aiStory.relevantNews} />
          </div>
        )}

        {!aiStory && !aiStoryLoading && event && (
          <div className="flex flex-col gap-4">
            {event.shortDescription && (
              <SectionBlock title="Öğrenme Notu" content={event.shortDescription} />
            )}
            {event.learningNote && (
              <div className="learning-note">
                <span className="material-symbols-outlined text-tertiary-container mt-0.5 text-[20px]" style={{ fontVariationSettings: "'FILL' 1" }}>
                  school
                </span>
                <div>
                  <p className="text-label-md text-on-tertiary-container mb-1">Buradan ne öğrenmeliyim?</p>
                  <p className="text-body-md text-on-surface-variant">{event.learningNote}</p>
                </div>
              </div>
            )}
          </div>
        )}

        <p className="text-xs text-outline italic border-t border-surface-container-high pt-4">
          Bu açıklama yatırım tavsiyesi değildir. Eğitim ve demo amaçlıdır.
        </p>
      </div>
    </div>
  )
}
