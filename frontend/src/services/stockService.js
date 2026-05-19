import api from './api.js'
import { toArray } from '../utils/toArray.js'

export const HERO_SYMBOL = 'THYAO'

function sortStocks(list) {
  if (!Array.isArray(list)) return []
  return [...list].sort((a, b) => {
    if (a.symbol === HERO_SYMBOL) return -1
    if (b.symbol === HERO_SYMBOL) return 1
    return a.symbol.localeCompare(b.symbol)
  })
}

export const stockService = {
  getStocks: () =>
    api.get('/stocks').then((r) => sortStocks(toArray(r?.data, ['stocks']))),
  getAll: () =>
    api.get('/stocks').then((r) => sortStocks(toArray(r?.data, ['stocks']))),
  getOne: (symbol) => api.get(`/stocks/${symbol}`).then((r) => r.data),
  getEvents: (symbol) =>
    api.get(`/stocks/${symbol}/events`).then((r) => toArray(r?.data, ['events'])),
  getPrices: (symbol, range = '30d') =>
    api.get(`/stocks/${symbol}/prices`, { params: { range } }).then((r) => {
      const d = r?.data
      if (!d || typeof d !== 'object') return { prices: [], dataSource: null }
      return {
        ...d,
        prices: toArray(d.prices, ['prices']),
      }
    }),
  getNews: (symbol, limit = 10) =>
    api
      .get(`/stocks/${symbol}/news`, { params: { limit } })
      .then((r) => toArray(r?.data, ['news', 'items'])),
  getNearestNews: (symbol, date, limit = 5) =>
    api
      .get(`/stocks/${symbol}/news/nearest`, { params: { date, limit } })
      .then((r) => toArray(r?.data, ['news', 'items'])),
}
