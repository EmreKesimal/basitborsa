export default function MetricCard({ label, value, hint }) {
  return (
    <div className="card card-hover p-stack-gap-sm flex flex-col gap-1">
      <span className="text-label-md text-outline">{label}</span>
      <span className="text-headline-md font-semibold text-on-surface">{value ?? '—'}</span>
      {hint && <span className="text-xs text-on-surface-variant">{hint}</span>}
    </div>
  )
}
