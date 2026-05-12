import TopNav from './TopNav.jsx'
import BottomNav from './BottomNav.jsx'

export default function AppShell({ children }) {
  return (
    <div className="min-h-screen flex flex-col bg-background">
      <TopNav />
      <main className="flex-grow w-full max-w-content mx-auto px-container-margin-mobile py-stack-gap-lg pb-28 md:pb-stack-gap-lg flex flex-col gap-stack-gap-lg">
        {children}
      </main>
      <BottomNav />
    </div>
  )
}
