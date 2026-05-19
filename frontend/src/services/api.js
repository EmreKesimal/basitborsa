import axios from 'axios'

// Resolve API base URL.
// - In dev: defaults to '/api' (Vite proxy forwards to Spring Boot on :8080).
// - In prod: set VITE_API_BASE_URL to the backend origin (e.g. https://api.example.com)
//   or include the /api suffix (e.g. https://api.example.com/api). Both forms work.
function resolveBaseURL() {
  const raw = (import.meta.env?.VITE_API_BASE_URL || '').trim()
  if (!raw) return '/api'
  // Strip trailing slashes to avoid '//api' or '/api/api'.
  const trimmed = raw.replace(/\/+$/, '')
  return /\/api$/.test(trimmed) ? trimmed : `${trimmed}/api`
}

const api = axios.create({
  baseURL: resolveBaseURL(),
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.response.use(
  (res) => res,
  (error) => {
    const message = error.response?.data?.message || error.message || 'Bağlantı hatası'
    return Promise.reject(new Error(message))
  }
)

export default api
