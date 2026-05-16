export default function MetricCard({ label, value, hint }) {
  return (
    <div className="card p-4 flex flex-col gap-1.5">
      <span className="text-xs text-outline uppercase tracking-wide">{label}</span>
      <span className="text-headline-md font-bold text-on-surface">{value ?? '—'}</span>
      {hint && <span className="text-xs text-on-surface-variant">{hint}</span>}
    </div>
  )
}
