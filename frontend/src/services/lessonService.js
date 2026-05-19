import api from './api.js'
import { toArray } from '../utils/toArray.js'

export const lessonService = {
  getAll: () =>
    api.get('/lessons').then((r) => toArray(r?.data, ['lessons'])),
  getOne: (slug) => api.get(`/lessons/${slug}`).then((r) => r.data),
}
