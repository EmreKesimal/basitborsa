import api from './api.js'

export const aiService = {
  explainEvent: (payload) => api.post('/ai/explain-event', payload).then((r) => r.data),
  explainTerm: (term) => api.post('/ai/explain-term', { term }).then((r) => r.data),
  explainStock: (symbol, companyName, sector, question) =>
    api.post('/ai/explain-stock', { symbol, companyName, sector, question }).then((r) => r.data),
}
