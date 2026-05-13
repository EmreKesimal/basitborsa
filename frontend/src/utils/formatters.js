export function formatCurrency(value, decimals = 2) {
  if (value == null) return '—'
  return new Intl.NumberFormat('tr-TR', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(value) + ' ₺'
}

export function formatPercent(value, decimals = 2) {
  if (value == null) return '—'
  const sign = value >= 0 ? '+' : ''
  return `${sign}${Number(value).toFixed(decimals)}%`
}

export function formatDate(dateStr) {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('tr-TR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })
}

export function isPositive(value) {
  return value != null && Number(value) > 0
}

export function changeColorClass(value) {
  if (value == null) return 'text-on-surface-variant'
  return Number(value) >= 0 ? 'text-positive' : 'text-error'
}
