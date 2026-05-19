import { NavLink } from 'react-router-dom'

const navItems = [
  { to: '/', label: 'Ana Sayfa', icon: 'home', end: true },
  { to: '/stocks', label: 'Hisseler', icon: 'show_chart' },
  { to: '/learn', label: 'Öğren', icon: 'school' },
]

export default function BottomNav() {
  return (
    <nav className="md:hidden fixed bottom-0 left-0 w-full z-50 flex justify-around items-center py-3 px-4 bg-surface border-t border-surface-container-high shadow-[0_-4px_20px_rgba(51,65,85,0.05)] pb-safe">
      {navItems.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          end={item.end}
          className={({ isActive }) =>
            `flex flex-col items-center justify-center transition-colors active:scale-90 transition-transform duration-150 ${
              isActive ? 'text-primary' : 'text-on-surface-variant'
            }`
          }
        >
          {({ isActive }) => (
            <>
              <span
                className="material-symbols-outlined text-[24px]"
                style={isActive ? { fontVariationSettings: "'FILL' 1" } : {}}
              >
                {item.icon}
              </span>
              <span className="text-xs font-semibold mt-1">{item.label}</span>
            </>
          )}
        </NavLink>
      ))}
    </nav>
  )
}
