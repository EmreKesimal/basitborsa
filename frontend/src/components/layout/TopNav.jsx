import { NavLink, useNavigate } from 'react-router-dom'
import { useDarkMode } from '../../hooks/useDarkMode.js'

const navItems = [
  { to: '/', label: 'Ana Sayfa', icon: 'home' },
  { to: '/stocks', label: 'Hisseler', icon: 'show_chart' },
  { to: '/learn', label: 'Öğren', icon: 'school' },
]

export default function TopNav() {
  const navigate = useNavigate()
  const [isDark, setIsDark] = useDarkMode()

  return (
    <header className="bg-surface-container-lowest border-b border-surface-container-high sticky top-0 z-50">
      <div className="flex items-center justify-between px-4 sm:px-6 h-16 w-full max-w-7xl mx-auto">
        <button
          onClick={() => navigate('/')}
          className="flex items-center gap-2 hover:opacity-80 transition-opacity"
        >
          <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center">
            <span className="material-symbols-outlined text-on-primary text-[18px]" style={{ fontVariationSettings: "'FILL' 1" }}>
              account_balance
            </span>
          </div>
          <span className="font-bold text-headline-md text-on-surface">BasitBorsa</span>
        </button>

        <div className="flex items-center gap-6">
          <nav className="hidden md:flex items-center gap-6">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/'}
                className={({ isActive }) =>
                  `text-label-md transition-colors ${
                    isActive
                      ? 'text-primary font-bold'
                      : 'text-on-surface-variant hover:text-on-surface'
                  }`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>

          <button
            onClick={() => setIsDark((d) => !d)}
            className="w-9 h-9 flex items-center justify-center rounded-full text-on-surface-variant hover:bg-surface-container transition-colors"
            aria-label={isDark ? 'Açık moda geç' : 'Koyu moda geç'}
          >
            <span className="material-symbols-outlined text-[20px]">
              {isDark ? 'light_mode' : 'dark_mode'}
            </span>
          </button>
        </div>
      </div>
    </header>
  )
}
