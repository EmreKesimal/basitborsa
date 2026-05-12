import api from './api.js'

export const stockService = {
  getAll: () => api.get('/stocks').then((r) => r.data),
  getOne: (symbol) => api.get(`/stocks/${symbol}`).then((r) => r.data),
  getEvents: (symbol) => api.get(`/stocks/${symbol}/events`).then((r) => r.data),
  getPrices: (symbol, range = '30d') =>
    api.get(`/stocks/${symbol}/prices`, { params: { range } }).then((r) => r.data),
}
