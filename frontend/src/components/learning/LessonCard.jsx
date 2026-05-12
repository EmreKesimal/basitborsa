import { useState } from 'react'

const ACCENT_COLORS = {
  'primary-container': 'bg-primary-container',
  'tertiary-container': 'bg-tertiary-container',
  'secondary-container': 'bg-secondary-container',
}

export default function LessonCard({ lesson }) {
  const [expanded, setExpanded] = useState(false)
  const accentClass = ACCENT_COLORS[lesson.accentColor] || 'bg-primary-container'

  return (
    <article className="card card-hover flex flex-col gap-4 relative overflow-hidden border border-outline-variant/20">
      <div className={`absolute top-0 left-0 w-full h-1 ${accentClass}`} />
      <div className="flex items-center gap-3 mt-1">
        <div className="w-10 h-10 rounded-full bg-surface-container flex items-center justify-center text-primary flex-shrink-0">
          <span className="material-symbols-outlined text-[20px]">{lesson.iconName || 'school'}</span>
        </div>
        <h2 className="text-headline-md font-semibold text-on-surface">{lesson.title}</h2>
      </div>

      <p className="text-body-md text-on-surface-variant flex-grow">{lesson.shortDescription}</p>

      {expanded && (
        <div className="flex flex-col gap-3 text-body-md text-on-surface-variant border-t border-surface-container-high pt-3">
          {lesson.content && <p>{lesson.content}</p>}
          {lesson.whyItMatters && (
            <div className="learning-note">
              <span className="material-symbols-outlined text-tertiary-container text-[18px] mt-0.5">lightbulb</span>
              <p>{lesson.whyItMatters}</p>
            </div>
          )}
          {lesson.exampleText && (
            <div className="bg-surface-container rounded-lg p-3">
              <p className="text-label-md text-on-surface-variant mb-1">Örnek</p>
              <p>{lesson.exampleText}</p>
            </div>
          )}
          {lesson.beginnerWarning && (
            <div className="flex items-start gap-2 text-tertiary">
              <span className="material-symbols-outlined text-[16px] mt-0.5">warning</span>
              <p className="text-sm">{lesson.beginnerWarning}</p>
            </div>
          )}
        </div>
      )}

      <button
        onClick={() => setExpanded((p) => !p)}
        className="text-primary text-label-md flex items-center gap-1 w-fit hover:opacity-80 transition-opacity"
      >
        {expanded ? 'Kapat' : 'Daha fazla oku'}
        <span className="material-symbols-outlined text-sm">
          {expanded ? 'keyboard_arrow_up' : 'arrow_forward'}
        </span>
      </button>
    </article>
  )
}
