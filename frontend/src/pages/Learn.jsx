import { useEffect, useState, useCallback } from 'react'
import { lessonService } from '../services/lessonService.js'
import LessonCard from '../components/learning/LessonCard.jsx'
import LoadingState from '../components/common/LoadingState.jsx'
import ErrorState from '../components/common/ErrorState.jsx'

export default function Learn() {
  const [lessons, setLessons] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await lessonService.getAll()
      setLessons(Array.isArray(data) ? data : [])
    } catch (err) {
      setError(err?.message || 'Dersler yüklenemedi.')
      setLessons([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const safeLessons = Array.isArray(lessons) ? lessons : []

  return (
    <div className="flex flex-col gap-stack-gap-lg">
      {/* Hero */}
      <section className="flex flex-col gap-stack-gap-md text-center items-center">
        <h1 className="text-display-lg font-bold text-on-surface">Borsayı Öğrenin</h1>
        <p className="text-body-lg text-on-surface-variant max-w-2xl">
          Karmaşık finansal terimleri geride bırakın. Basit, anlaşılır ve adım adım eğitimlerle finansal okuryazarlığınızı artırın.
        </p>
      </section>

      {loading && <LoadingState message="Dersler yükleniyor..." />}
      {!loading && error && <ErrorState message={error} onRetry={load} />}

      {!loading && !error && safeLessons.length > 0 && (
        <section className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-stack-gap-md pb-stack-gap-lg">
          {safeLessons.map((lesson) => (
            <LessonCard key={lesson.id} lesson={lesson} />
          ))}
        </section>
      )}

      {!loading && !error && safeLessons.length === 0 && (
        <div className="text-center py-12 text-on-surface-variant">
          <span className="material-symbols-outlined text-4xl">school</span>
          <p className="mt-2 text-body-md">Henüz ders eklenmemiş.</p>
        </div>
      )}
    </div>
  )
}
