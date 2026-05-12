import { useState } from 'react'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, ReferenceDot,
} from 'recharts'
import { formatCurrency, formatDate } from '../../utils/formatters.js'

const RANGES = [
  { label: '7G', value: '7d' },
  { label: '30G', value: '30d' },
  { label: '90G', value: '90d' },
  { label: '6A', value: '180d' },
  { label: '1Y', value: '1y' },
]

function CustomTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null
  return (
    <div className="bg-surface-container-lowest rounded-lg p-3 shadow-card border border-surface-container-high text-sm">
      <p className="text-on-surface-variant mb-1">{formatDate(label)}</p>
      <p className="text-on-surface font-semibold">{formatCurrency(payload[0].value)}</p>
    </div>
  )
}

export default function StockChart({ priceHistory, events, onEventClick, selectedRange, onRangeChange }) {
  const prices = priceHistory?.prices || []
  const eventDates = new Set(events?.map((e) => e.eventDate) || [])

  const data = prices.map((p) => ({
    date: p.date,
    price: Number(p.close),
    hasEvent: eventDates.has(p.date),
  }))

  const eventPoints = events?.map((e) => {
    const pricePoint = data.find((d) => d.date === e.eventDate)
    return { ...e, price: pricePoint?.price }
  }).filter((e) => e.price != null) || []

  return (
    <div className="card p-stack-gap-md flex flex-col gap-stack-gap-sm">
      <div className="flex justify-between items-center">
        <h3 className="text-headline-md font-semibold text-on-surface">Fiyat Geçmişi</h3>
        <div className="flex gap-1">
          {RANGES.map((r) => (
            <button
              key={r.value}
              onClick={() => onRangeChange(r.value)}
              className={`px-3 py-1 rounded-lg text-label-md transition-colors ${
                selectedRange === r.value
                  ? 'bg-surface-container-low text-primary font-semibold'
                  : 'text-on-surface-variant hover:bg-surface-container-low'
              }`}
            >
              {r.label}
            </button>
          ))}
        </div>
      </div>

      {data.length === 0 ? (
        <div className="h-48 flex items-center justify-center text-on-surface-variant text-body-md">
          Fiyat verisi bulunamadı.
        </div>
      ) : (
        <ResponsiveContainer width="100%" height={240}>
          <LineChart data={data} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#dde9ff" />
            <XAxis
              dataKey="date"
              tick={{ fontSize: 11, fill: '#6d797c' }}
              tickFormatter={(d) => new Date(d).toLocaleDateString('tr-TR', { month: 'short', day: 'numeric' })}
              interval="preserveStartEnd"
            />
            <YAxis
              tick={{ fontSize: 11, fill: '#6d797c' }}
              tickFormatter={(v) => `₺${v.toFixed(0)}`}
              domain={['auto', 'auto']}
              width={55}
            />
            <Tooltip content={<CustomTooltip />} />
            <Line
              type="monotone"
              dataKey="price"
              stroke="#006876"
              strokeWidth={2}
              dot={false}
              activeDot={{ r: 5, fill: '#006876' }}
            />
            {eventPoints.map((e) => (
              <ReferenceDot
                key={e.id}
                x={e.eventDate}
                y={e.price}
                r={7}
                fill={e.eventType === 'RISE' ? '#006876' : e.eventType === 'FALL' ? '#ba1a1a' : '#da8a36'}
                stroke="#ffffff"
                strokeWidth={2}
                style={{ cursor: 'pointer' }}
                onClick={() => onEventClick && onEventClick(e)}
                label={{ value: '●', position: 'top', fontSize: 8 }}
              />
            ))}
          </LineChart>
        </ResponsiveContainer>
      )}

      {eventPoints.length > 0 && (
        <div className="flex flex-wrap gap-2 mt-1">
          <span className="text-xs text-on-surface-variant">Önemli olaylar:</span>
          <div className="flex gap-2 flex-wrap">
            <span className="flex items-center gap-1 text-xs text-green-600"><span className="w-2 h-2 rounded-full bg-primary inline-block" />Yükseliş</span>
            <span className="flex items-center gap-1 text-xs text-error"><span className="w-2 h-2 rounded-full bg-error inline-block" />Düşüş</span>
            <span className="flex items-center gap-1 text-xs text-tertiary"><span className="w-2 h-2 rounded-full bg-tertiary-container inline-block" />Tarafsız</span>
          </div>
        </div>
      )}

      <p className="text-xs text-outline">
        Veriler gecikmeli/gün sonu olabilir. Bu platform yatırım tavsiyesi vermez.
      </p>
    </div>
  )
}
