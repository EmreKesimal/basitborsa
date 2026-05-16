import TopNav from './TopNav.jsx'
import BottomNav from './BottomNav.jsx'

export default function AppShell({ children }) {
  return (
    <div className="min-h-screen flex flex-col bg-background">
      <TopNav />
      <main className="flex-grow w-full max-w-7xl mx-auto px-4 sm:px-6 py-8 pb-28 md:pb-10 flex flex-col gap-8">
        {children}
      </main>
      <BottomNav />
    </div>
  )
}
