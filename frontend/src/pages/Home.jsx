import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import LottieAnimation from '../components/common/LottieAnimation.jsx'

const STEPS = [
  {
    num: '01',
    icon: 'find_in_page',
    title: 'Hisseyi seç',
    desc: 'BIST hisselerini incele. Sade açıklamalarla ne iş yaptıklarını anla.',
    color: 'bg-primary text-on-primary',
  },
  {
    num: '02',
    icon: 'auto_stories',
    title: 'Grafiğin hikâyesini gör',
    desc: 'Grafikteki olaylara tıkla. Yapay zekâ o dönemde ne olmuş olabileceğini anlatır.',
    color: 'bg-tertiary-container text-on-tertiary-container',
  },
  {
    num: '03',
    icon: 'school',
    title: 'Kavramları öğren',
    desc: 'F/K, PD/DD, temettü gibi terimleri AI Öğretmen\'den öğren. Borsayı anla, önce anla.',
    color: 'bg-secondary-container text-on-secondary-container',
  },
]

const FEATURED = ['THYAO', 'ASELS', 'BIMAS', 'SISE', 'TUPRS']

export default function Home() {
  const [search, setSearch] = useState('')
  const navigate = useNavigate()

  function handleSearch(e) {
    e.preventDefault()
    const q = search.trim().toUpperCase()
    if (q) navigate(`/stocks/${q}`)
    else navigate('/stocks')
  }

  return (
    <div className="flex flex-col gap-16">
      {/* ── Hero ──────────────────────────────────────────── */}
      <section className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center pt-6">
        {/* Left: copy */}
        <div className="flex flex-col gap-7">
          <div className="inline-flex items-center gap-2 bg-primary-container bg-opacity-20 text-primary text-xs font-semibold px-3 py-1.5 rounded-full border border-primary border-opacity-20 w-fit">
            <span className="material-symbols-outlined text-[14px]">school</span>
            Yeni Başlayanlar İçin
          </div>

          <div>
            <h1 className="text-display-lg font-bold text-on-background leading-tight">
              Hisseyi alma,
            </h1>
            <h1 className="text-display-lg font-bold text-primary leading-tight">
              önce anla.
            </h1>
          </div>

          <p className="text-body-lg text-on-surface-variant max-w-lg">
            Yapay zekâ destekli borsa öğrenme platformu. Gerçek para riski olmadan,
            hisse grafiklerinin arkasındaki hikâyeleri keşfet.
          </p>

          <div className="flex flex-col sm:flex-row gap-3">
            <button
              onClick={() => navigate('/stocks')}
              className="btn-primary px-8 py-3.5 text-base"
            >
              <span className="material-symbols-outlined text-[20px]">show_chart</span>
              Hisseleri İncele
            </button>
            <button
              onClick={() => navigate('/learn')}
              className="btn-outline px-8 py-3.5 text-base"
            >
              <span className="material-symbols-outlined text-[20px]">school</span>
              Öğrenmeye Başla
            </button>
          </div>

          {/* Quick search */}
          <form onSubmit={handleSearch} className="relative max-w-md">
            <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline text-[18px]">
              search
            </span>
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Hisse ara (örn. THYAO, ASELS)"
              className="w-full h-12 pl-11 pr-24 bg-surface-container-lowest border border-surface-container-high rounded-xl text-body-md text-on-surface placeholder:text-outline focus:ring-2 focus:ring-primary focus:border-transparent outline-none shadow-card"
            />
            <button
              type="submit"
              className="absolute right-2 top-1/2 -translate-y-1/2 bg-primary text-on-primary text-label-md px-4 py-1.5 rounded-lg hover:opacity-90 transition-opacity"
            >
              Ara
            </button>
          </form>
        </div>

        {/* Right: Lottie animation */}
        <div className="flex items-center justify-center lg:justify-end">
          <div className="relative w-full max-w-xs">
            <div className="absolute inset-0 bg-primary rounded-3xl opacity-5 blur-3xl scale-110" />
            <div className="relative bg-surface-container-lowest rounded-3xl border border-surface-container-high shadow-card-hover p-8 flex flex-col items-center gap-4">
              <LottieAnimation
                src="/animations/finance-chart.json"
                className="w-44 h-24"
                loop
              />
              <div className="text-center">
                <p className="text-label-md text-primary font-bold">Grafiğin Hikâyesi</p>
                <p className="text-xs text-on-surface-variant mt-1">
                  Her fiyat hareketinin bir sebebi var
                </p>
              </div>
              <div className="flex gap-2 flex-wrap justify-center">
                {['THYAO +5.9%', 'ASELS +2.1%', 'BIMAS -1.3%'].map((t) => (
                  <span key={t} className="text-xs bg-surface-container-low px-2.5 py-1 rounded-full text-on-surface-variant border border-surface-container-high">
                    {t}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ── How it works ──────────────────────────────────── */}
      <section className="flex flex-col gap-8">
        <div className="text-center">
          <h2 className="text-headline-lg font-bold text-on-surface">Nasıl Çalışır?</h2>
          <p className="text-body-md text-on-surface-variant mt-2">
            Üç adımda borsayı anlamaya başla.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {STEPS.map((step) => (
            <div key={step.num} className="card p-6 flex flex-col gap-4 border border-surface-container-high">
              <div className="flex items-center gap-3">
                <div className={`w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0 ${step.color}`}>
                  <span className="material-symbols-outlined text-[20px]" style={{ fontVariationSettings: "'FILL' 1" }}>
                    {step.icon}
                  </span>
                </div>
                <span className="text-headline-lg font-black text-surface-container-highest opacity-70">
                  {step.num}
                </span>
              </div>
              <div>
                <h3 className="text-headline-md font-bold text-on-surface mb-2">{step.title}</h3>
                <p className="text-body-md text-on-surface-variant">{step.desc}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ── Featured stocks ───────────────────────────────── */}
      <section className="bg-surface-container-lowest rounded-2xl border border-surface-container-high p-6 sm:p-8 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
        <div>
          <h3 className="text-headline-md font-bold text-on-surface mb-1">Desteklenen Hisseler</h3>
          <p className="text-body-md text-on-surface-variant">BIST'ten seçili hisseler — gecikmeli/gün sonu verilerle.</p>
        </div>
        <div className="flex flex-wrap gap-2">
          {FEATURED.map((s) => (
            <button
              key={s}
              onClick={() => navigate(`/stocks/${s}`)}
              className="bg-surface-container px-4 py-2 rounded-xl border border-surface-container-high text-label-md text-primary font-bold hover:bg-primary hover:text-on-primary hover:border-primary transition-colors shadow-sm"
            >
              {s}
            </button>
          ))}
        </div>
      </section>

      {/* ── Disclaimer ────────────────────────────────────── */}
      <p className="text-xs text-outline text-center pb-2">
        Bu platform yatırım tavsiyesi vermez. Gösterilen bilgiler eğitim ve demo amaçlıdır.
        Veriler gecikmeli/gün sonu olabilir.
      </p>
    </div>
  )
}
