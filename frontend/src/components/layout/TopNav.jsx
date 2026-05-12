import { NavLink, useNavigate } from 'react-router-dom'

const navItems = [
  { to: '/', label: 'Ana Sayfa', icon: 'home' },
  { to: '/stocks', label: 'Hisseler', icon: 'show_chart' },
  { to: '/learn', label: 'Öğren', icon: 'school' },
  { to: '/portfolio', label: 'Portföyüm', icon: 'account_balance_wallet' },
]

export default function TopNav() {
  const navigate = useNavigate()

  return (
    <header className="bg-surface shadow-card sticky top-0 z-50">
      <div className="flex items-center justify-between px-container-margin-mobile h-16 w-full max-w-content mx-auto">
        <button
          onClick={() => navigate('/')}
          className="flex items-center gap-unit hover:opacity-80 transition-opacity"
        >
          <span className="material-symbols-outlined text-primary text-2xl" style={{ fontVariationSettings: "'FILL' 1" }}>
            account_balance
          </span>
          <span className="font-bold text-headline-md text-primary">BasitBorsa</span>
        </button>

        <nav className="hidden md:flex items-center gap-stack-gap-md">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) =>
                `text-label-md font-semibold transition-opacity hover:opacity-80 pb-1 ${
                  isActive
                    ? 'text-primary border-b-2 border-primary'
                    : 'text-on-surface-variant'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </div>
    </header>
  )
}
