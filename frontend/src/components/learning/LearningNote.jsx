export default function LearningNote({ title, children }) {
  return (
    <div className="learning-note">
      <span className="material-symbols-outlined text-tertiary-container mt-0.5 text-[20px]">lightbulb</span>
      <div>
        {title && <p className="text-label-md text-on-tertiary-container mb-1">{title}</p>}
        <div className="text-body-md text-on-surface-variant">{children}</div>
      </div>
    </div>
  )
}
