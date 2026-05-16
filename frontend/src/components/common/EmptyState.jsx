export default function EmptyState({ icon = 'search_off', message = 'Sonuç bulunamadı.' }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 gap-3 text-on-surface-variant">
      <span className="material-symbols-outlined text-5xl text-outline">{icon}</span>
      <p className="text-body-md text-center max-w-xs">{message}</p>
    </div>
  )
}
