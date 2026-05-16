const META = {
  EXTERNAL_PROVIDER: { label: 'Canlı Veri', cls: 'text-green-700 bg-green-50 border-green-200' },
  CACHED:            { label: 'Önbellekten', cls: 'text-blue-700 bg-blue-50 border-blue-200' },
  FALLBACK_SEED:     { label: 'Örnek Veri',  cls: 'text-orange-700 bg-orange-50 border-orange-200' },
}

export default function DataSourceBadge({ source }) {
  const m = META[source]
  if (!m) return null
  return (
    <span className={`inline-flex items-center text-xs px-2 py-0.5 rounded-full font-medium border ${m.cls}`}>
      {m.label}
    </span>
  )
}
