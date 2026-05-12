import api from './api.js'

export const portfolioService = {
  get: () => api.get('/portfolio').then((r) => r.data),
  addItem: (symbol, quantity) =>
    api.post('/portfolio/items', { symbol, quantity }).then((r) => r.data),
  removeItem: (id) => api.delete(`/portfolio/items/${id}`).then((r) => r.data),
}
