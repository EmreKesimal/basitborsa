import { Routes, Route } from 'react-router-dom'
import AppShell from '../components/layout/AppShell.jsx'
import Home from '../pages/Home.jsx'
import Stocks from '../pages/Stocks.jsx'
import StockDetail from '../pages/StockDetail.jsx'
import Learn from '../pages/Learn.jsx'
import Portfolio from '../pages/Portfolio.jsx'

export default function App() {
  return (
    <AppShell>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/stocks" element={<Stocks />} />
        <Route path="/stocks/:symbol" element={<StockDetail />} />
        <Route path="/learn" element={<Learn />} />
        <Route path="/portfolio" element={<Portfolio />} />
      </Routes>
    </AppShell>
  )
}
