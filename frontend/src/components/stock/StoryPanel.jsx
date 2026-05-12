import { formatDate, formatPercent } from '../../utils/formatters.js'

export default function StoryPanel({ event }) {
  if (!event) {
    return (
      <div className="card overflow-hidden">
        <div className="h-1 w-full bg-[#FFF9C4]" />
        <div className="p-stack-gap-md text-center py-12">
          <span className="material-symbols-outlined text-4xl text-outline">auto_stories</span>
          <p className="text-body-md text-on-surface-variant mt-2">
            Grafikte bir olayı seçerek hikâyesini okuyun.
          </p>
        </div>
      </div>
    )
  }

  const isRise = event.eventType === 'RISE'
  const typeColor = isRise ? 'text-green-600' : event.eventType === 'FALL' ? 'text-error' : 'text-tertiary'
  const typeLabel = isRise ? 'Yükseliş' : event.eventType === 'FALL' ? 'Düşüş' : 'Tarafsız'

  return (
    <div className="card overflow-hidden">
      <div className="h-1 w-full bg-[#FFF9C4]" />
      <div className="p-stack-gap-md flex flex-col gap-stack-gap-sm">
        <div className="flex items-center gap-unit">
          <span className="material-symbols-outlined text-tertiary-container text-[22px]">auto_stories</span>
          <h3 className="text-headline-md font-semibold text-on-surface">Grafiğin Hikâyesi</h3>
        </div>

        <div className="flex items-start justify-between gap-2">
          <div>
            <h4 className="text-body-md font-semibold text-on-surface">{event.title}</h4>
            <p className="text-xs text-on-surface-variant">{formatDate(event.eventDate)}</p>
          </div>
          <span className={`text-label-md font-semibold ${typeColor} flex-shrink-0`}>
            {formatPercent(event.priceChangePercent)} · {typeLabel}
          </span>
        </div>

        <div className="w-full h-px bg-outline-variant opacity-30" />

        <div>
          <h5 className="text-label-md text-on-surface-variant mb-1">Ne Oldu?</h5>
          <p className="text-body-md text-on-surface">{event.shortDescription}</p>
        </div>

        {event.relatedNews && (
          <div>
            <h5 className="text-label-md text-on-surface-variant mb-1">İlgili Gelişmeler</h5>
            <p className="text-body-md text-on-surface">{event.relatedNews}</p>
          </div>
        )}

        {event.learningNote && (
          <div className="learning-note mt-2">
            <span className="material-symbols-outlined text-tertiary-container mt-0.5 text-[20px]">school</span>
            <div>
              <p className="text-label-md text-on-tertiary-container mb-1">Buradan ne öğrenmeliyim?</p>
              <p className="text-body-md text-on-surface-variant">{event.learningNote}</p>
            </div>
          </div>
        )}

        <p className="text-xs text-outline italic mt-1">
          Bu açıklama yatırım tavsiyesi değildir. Eğitim ve demo amaçlıdır.
        </p>
      </div>
    </div>
  )
}
