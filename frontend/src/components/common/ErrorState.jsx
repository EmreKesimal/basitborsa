export default function ErrorState({ message = 'Bir hata oluştu.', onRetry }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 gap-4 text-center">
      <span className="material-symbols-outlined text-error text-5xl">error_outline</span>
      <p className="text-body-md text-on-surface-variant max-w-sm">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="text-primary text-label-md font-semibold hover:opacity-80 transition-opacity flex items-center gap-1"
        >
          <span className="material-symbols-outlined text-[16px]">refresh</span>
          Tekrar dene
        </button>
      )}
    </div>
  )
}
