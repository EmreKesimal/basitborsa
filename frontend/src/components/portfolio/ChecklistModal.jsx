import { useState } from 'react'

const CHECKLIST_ITEMS = [
  'Bu şirketin ne iş yaptığını biliyor musun?',
  'Son haberleri inceledin mi?',
  'Temel göstergelere (F/K, PD/DD) baktın mı?',
  'Grafikteki önemli hareketleri anladın mı?',
  'Riskleri okudum ve kabul ediyorum.',
  'Bu işlemi neden yaptığımı açıklayabilirim.',
]

export default function ChecklistModal({ stock, onConfirm, onClose }) {
  const [checked, setChecked] = useState({})
  const [quantity, setQuantity] = useState(1)

  const allChecked = CHECKLIST_ITEMS.every((_, i) => checked[i])

  function toggle(i) {
    setChecked((prev) => ({ ...prev, [i]: !prev[i] }))
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
      <div className="bg-surface-container-lowest rounded-xl shadow-card max-w-md w-full max-h-[90vh] overflow-y-auto">
        <div className="p-stack-gap-md flex flex-col gap-stack-gap-sm">
          <div className="flex items-center justify-between">
            <h2 className="text-headline-md font-semibold text-on-surface">Karar Öncesi Kontrol</h2>
            <button onClick={onClose} className="text-on-surface-variant hover:text-on-surface">
              <span className="material-symbols-outlined">close</span>
            </button>
          </div>

          <div className="learning-note">
            <span className="material-symbols-outlined text-tertiary-container text-[20px] mt-0.5">lightbulb</span>
            <p className="text-body-md text-on-surface-variant">
              Bu liste seni düşünmeye teşvik etmek içindir. Tüm kutucukları işaretlemek zorunlu değildir ama önerilir.
            </p>
          </div>

          <div className="flex flex-col gap-2">
            {CHECKLIST_ITEMS.map((item, i) => (
              <label
                key={i}
                className="flex items-center gap-3 p-2 hover:bg-surface-container-low rounded-lg transition-colors cursor-pointer"
              >
                <input
                  type="checkbox"
                  checked={!!checked[i]}
                  onChange={() => toggle(i)}
                  className="w-5 h-5 rounded border-outline text-primary accent-primary"
                />
                <span className="text-body-md text-on-surface">{item}</span>
              </label>
            ))}
          </div>

          <div className="flex items-center gap-3">
            <label className="text-label-md text-on-surface-variant whitespace-nowrap">Adet:</label>
            <input
              type="number"
              min={1}
              value={quantity}
              onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
              className="w-24 border border-outline-variant rounded-lg px-3 py-2 text-body-md text-on-surface bg-surface-container-low focus:ring-2 focus:ring-primary outline-none"
            />
            <span className="text-body-md text-on-surface-variant">
              ≈ {stock?.currentPrice ? (Number(stock.currentPrice) * quantity).toFixed(2) + ' ₺' : '—'}
            </span>
          </div>

          <button
            onClick={() => onConfirm(quantity)}
            className="btn-primary w-full mt-2"
          >
            <span className="material-symbols-outlined text-[18px]">add_circle</span>
            Sanal Portföye Ekle
          </button>

          <p className="disclaimer">
            Bu işlem sadece sanal portföy içindir. Gerçek para içermez.
          </p>
        </div>
      </div>
    </div>
  )
}
