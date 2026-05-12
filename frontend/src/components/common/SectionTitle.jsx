export default function SectionTitle({ children, className = '' }) {
  return (
    <h2 className={`text-headline-lg font-semibold text-on-surface border-b border-surface-container-high pb-unit ${className}`}>
      {children}
    </h2>
  )
}
