export default function Card({ children, className = '', hoverable = false, onClick }) {
  return (
    <div
      className={`card p-stack-gap-md ${hoverable ? 'card-hover cursor-pointer' : ''} ${className}`}
      onClick={onClick}
    >
      {children}
    </div>
  )
}
