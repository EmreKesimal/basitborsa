import api from './api.js'

export const HERO_SYMBOL = 'THYAO'

function sortStocks(list) {
  if (!Array.isArray(list)) return list
  return [...list].sort((a, b) => {
    if (a.symbol === HERO_SYMBOL) return -1
    if (b.symbol === HERO_SYMBOL) return 1
    return a.symbol.localeCompare(b.symbol)
  })
}

export const stockService = {
  getAll: () => api.get('/stocks').then((r) => sortStocks(r.data)),
  getOne: (symbol) => api.get(`/stocks/${symbol}`).then((r) => r.data),
  getEvents: (symbol) => api.get(`/stocks/${symbol}/events`).then((r) => r.data),
  getPrices: (symbol, range = '30d') =>
    api.get(`/stocks/${symbol}/prices`, { params: { range } }).then((r) => r.data),
  getNews: (symbol, limit = 10) =>
    api.get(`/stocks/${symbol}/news`, { params: { limit } }).then((r) => r.data),
  getNearestNews: (symbol, date, limit = 5) =>
    api.get(`/stocks/${symbol}/news/nearest`, { params: { date, limit } }).then((r) => r.data),
}
