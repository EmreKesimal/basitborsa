export default function Button({ children, variant = 'primary', className = '', disabled = false, onClick, type = 'button' }) {
  const base = variant === 'primary' ? 'btn-primary' : 'btn-outline'
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={`${base} ${disabled ? 'opacity-50 cursor-not-allowed' : ''} ${className}`}
    >
      {children}
    </button>
  )
}
