import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

const STEPS = [
  {
    num: 1,
    icon: 'find_in_page',
    title: 'Hisseyi seç',
    desc: 'Merak ettiğin şirketi bul. Sade özetlerle ne iş yaptıklarını anla.',
    accent: 'bg-secondary-fixed',
  },
  {
    num: 2,
    icon: 'insights',
    title: 'Grafiğin hikâyesini gör',
    desc: 'Yapay zekâ destekli analizlerle fiyat hareketlerinin arkasındaki sebepleri öğren.',
    accent: 'bg-tertiary-fixed-dim',
  },
  {
    num: 3,
    icon: 'account_balance_wallet',
    title: 'Sanal portföyde pratik yap',
    desc: 'Öğrendiklerini risk almadan test et. Kendi stratejini oluştur.',
    accent: 'bg-primary-container',
  },
]

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
    <div className="flex flex-col gap-stack-gap-lg">
      {/* Hero */}
      <section className="flex flex-col items-center text-center gap-stack-gap-md pt-stack-gap-md">
        <h1 className="text-display-lg font-bold text-on-background max-w-2xl leading-tight">
          Hisseyi alma, önce anla.
        </h1>
        <p className="text-body-lg text-on-surface-variant max-w-xl">
          Gerçek para riski olmadan, yapay zekâ ile borsayı öğren. Sadeleştirilmiş verilerle finansal okuryazarlığını artır.
        </p>

        {/* Search */}
        <form onSubmit={handleSearch} className="w-full max-w-lg relative shadow-card-hover rounded-lg">
          <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline">search</span>
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Hisse senedi ara (örn. THYAO, ASELS)"
            className="w-full h-14 pl-12 pr-24 bg-surface-container-low border-none rounded-lg text-body-md text-on-surface placeholder:text-outline focus:ring-2 focus:ring-primary outline-none"
          />
          <button
            type="submit"
            className="absolute right-2 top-1/2 -translate-y-1/2 bg-surface-container text-primary text-label-md px-3 py-1.5 rounded-md hover:bg-surface-container-high transition-colors"
          >
            Ara
          </button>
        </form>

        <button
          onClick={() => navigate('/stocks')}
          className="btn-primary"
        >
          Borsayı Öğrenmeye Başla
          <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
        </button>
      </section>

      {/* Steps */}
      <section className="grid grid-cols-1 md:grid-cols-3 gap-stack-gap-md">
        {STEPS.map((step) => (
          <div
            key={step.num}
            className="card card-hover relative overflow-hidden border border-surface-container-high"
          >
            <div className={`absolute top-0 left-0 w-full h-2 ${step.accent}`} />
            <div className="w-12 h-12 bg-surface-container rounded-full flex items-center justify-center mb-stack-gap-sm mt-2">
              <span
                className="material-symbols-outlined text-primary text-[28px]"
                style={{ fontVariationSettings: "'FILL' 1" }}
              >
                {step.icon}
              </span>
            </div>
            <h3 className="text-headline-md font-semibold text-on-background mb-unit">
              {step.num}. {step.title}
            </h3>
            <p className="text-body-md text-on-surface-variant">{step.desc}</p>
          </div>
        ))}
      </section>

      {/* Market overview teaser */}
      <section className="border-t border-surface-container-high pt-stack-gap-md">
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between bg-surface-container-low rounded-xl p-stack-gap-md gap-stack-gap-sm">
          <div>
            <h4 className="text-headline-md font-semibold text-on-background mb-unit">Desteklenen Hisseler</h4>
            <p className="text-body-md text-on-surface-variant">BIST'ten seçili hisseler. Gecikmeli/gün sonu verilerle.</p>
          </div>
          <div className="flex flex-wrap gap-2">
            {['THYAO', 'ASELS', 'BIMAS', 'SISE', 'TUPRS'].map((s) => (
              <button
                key={s}
                onClick={() => navigate(`/stocks/${s}`)}
                className="bg-surface-container-lowest px-4 py-2 rounded-lg border border-surface-container-high shadow-sm text-label-md text-primary font-semibold hover:bg-surface-container transition-colors"
              >
                {s}
              </button>
            ))}
          </div>
        </div>
      </section>

      <p className="text-xs text-outline text-center">
        Bu platform yatırım tavsiyesi vermez. Gösterilen bilgiler eğitim ve demo amaçlıdır.
        Veriler gecikmeli/gün sonu olabilir.
      </p>
    </div>
  )
}
