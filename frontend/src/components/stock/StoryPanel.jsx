import { formatDate, formatPercent } from '../../utils/formatters.js'

const SOURCE_BADGE = {
  ai_generated: { label: 'Yapay Zekâ', cls: 'text-green-700 bg-green-50 border-green-200' },
  fallback:     { label: 'Statik İçerik', cls: 'text-orange-700 bg-orange-50 border-orange-200' },
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

export default function StoryPanel({ event, aiStory, aiStoryLoading }) {
  if (!event) {
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
              Grafikteki renkli bir noktaya tıkla ve o dönemde ne olmuş olabileceğini öğren.
            </p>
          </div>
          <div className="flex items-center gap-2 text-xs text-outline bg-surface-container-low rounded-lg px-3 py-2">
            <span className="material-symbols-outlined text-[14px]">touch_app</span>
            Olaya tıkla → Hikâyeyi gör
          </div>
        </div>
      </div>
    )
  }

  const isRise = event.eventType === 'RISE'
  const typeColor = isRise ? 'text-positive' : event.eventType === 'FALL' ? 'text-error' : 'text-tertiary'
  const typeLabel = isRise ? 'Yükseliş' : event.eventType === 'FALL' ? 'Düşüş' : 'Tarafsız'
  const badge = aiStory ? SOURCE_BADGE[aiStory.sourceType] : null

  return (
    <div className="card overflow-hidden">
      <div className="h-1 w-full bg-gradient-to-r from-primary to-tertiary-container" />
      <div className="p-6 flex flex-col gap-5">

        {/* Panel title */}
        <div className="flex items-center gap-2">
          <span className="material-symbols-outlined text-primary text-[20px]" style={{ fontVariationSettings: "'FILL' 1" }}>
            auto_stories
          </span>
          <h3 className="text-headline-md font-bold text-on-surface">Grafiğin Hikâyesi</h3>
        </div>

        {/* Event header */}
        <div className="bg-surface-container-low rounded-xl p-4 flex items-start justify-between gap-3">
          <div className="min-w-0">
            <h4 className="text-body-md font-bold text-on-surface">{event.title}</h4>
            <p className="text-xs text-outline mt-0.5">{formatDate(event.eventDate)}</p>
          </div>
          <div className="text-right flex-shrink-0">
            <span className={`text-label-md font-bold ${typeColor}`}>
              {formatPercent(event.priceChangePercent)}
            </span>
            <p className={`text-xs mt-0.5 ${typeColor}`}>{typeLabel}</p>
          </div>
        </div>

        <div className="w-full h-px bg-outline-variant opacity-30" />

        {/* Loading */}
        {aiStoryLoading && (
          <div className="flex flex-col items-center gap-3 py-6">
            <div className="w-5 h-5 border-2 border-primary border-t-transparent rounded-full animate-spin" />
            <p className="text-body-md text-on-surface-variant">Yapay zekâ analiz ediyor...</p>
          </div>
        )}

        {/* AI Story */}
        {aiStory && !aiStoryLoading && (
          <div className="flex flex-col gap-5">
            {badge && (
              <span className={`text-xs px-2.5 py-1 rounded-full font-medium border w-fit ${badge.cls}`}>
                {badge.label}
              </span>
            )}

            {aiStory.summary && (
              <p className="text-body-md text-on-surface font-medium leading-relaxed">
                {aiStory.summary}
              </p>
            )}

            {aiStory.sections?.map((s, i) => (
              <SectionBlock key={i} title={s.title} content={s.content} />
            ))}
          </div>
        )}

        {/* Fallback static */}
        {!aiStory && !aiStoryLoading && (
          <div className="flex flex-col gap-4">
            {event.shortDescription && (
              <SectionBlock title="Ne Oldu?" content={event.shortDescription} />
            )}
            {event.relatedNews && (
              <SectionBlock title="İlgili Gelişmeler" content={event.relatedNews} />
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
