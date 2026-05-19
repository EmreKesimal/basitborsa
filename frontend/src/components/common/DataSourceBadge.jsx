const META = {
  EXTERNAL_PROVIDER: { label: 'Gecikmeli/Gün Sonu Veri', cls: 'text-green-700 bg-green-50 border-green-200' },
  CACHED:            { label: 'Önbellekten',             cls: 'text-blue-700 bg-blue-50 border-blue-200' },
  CACHED_EXTERNAL:   { label: 'Önbellekten',             cls: 'text-blue-700 bg-blue-50 border-blue-200' },
  EXTERNAL_NEWS:     { label: 'Gerçek Haber',            cls: 'text-green-700 bg-green-50 border-green-200' },
  CACHED_NEWS:       { label: 'Önbellek Haber',          cls: 'text-blue-700 bg-blue-50 border-blue-200' },
  KAP:               { label: 'KAP',                     cls: 'text-purple-700 bg-purple-50 border-purple-200' },
  UNAVAILABLE:       { label: 'Veri Yok',                cls: 'text-orange-700 bg-orange-50 border-orange-200' },
  DEMO_LIMITED:      { label: 'Demo Kapsamı',            cls: 'text-amber-700 bg-amber-50 border-amber-200' },
  // Legacy — should not appear in active flow.
  FALLBACK_SEED:     { label: 'Örnek Veri',              cls: 'text-orange-700 bg-orange-50 border-orange-200' },
  SEED:              { label: 'Örnek Veri',              cls: 'text-orange-700 bg-orange-50 border-orange-200' },
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
