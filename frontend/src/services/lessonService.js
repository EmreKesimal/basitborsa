import api from './api.js'

export const lessonService = {
  getAll: () => api.get('/lessons').then((r) => r.data),
  getOne: (slug) => api.get(`/lessons/${slug}`).then((r) => r.data),
}
