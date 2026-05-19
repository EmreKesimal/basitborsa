import { useState } from 'react'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, ReferenceDot,
} from 'recharts'
import { formatCurrency, formatDate } from '../../utils/formatters.js'
import DataSourceBadge from '../common/DataSourceBadge.jsx'

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
    <div className="bg-surface-container-lowest rounded-xl p-3 shadow-card-hover border border-surface-container-high text-sm">
      <p className="text-on-surface-variant mb-1">{formatDate(label)}</p>
      <p className="text-on-surface font-bold text-base">{formatCurrency(payload[0].value)}</p>
    </div>
  )
}

export default function StockChart({
  priceHistory, events, onEventClick, onChartClick, selectedRange, onRangeChange, selectedEvent,
}) {
  const prices = Array.isArray(priceHistory?.prices) ? priceHistory.prices : []
  const safeEvents = Array.isArray(events) ? events : []
  const dataSource = priceHistory?.dataSource
  const eventDates = new Set(safeEvents.map((e) => e.eventDate))

  const data = prices.map((p) => ({
    date: p.date,
    price: Number(p.close),
    hasEvent: eventDates.has(p.date),
  }))

  const eventPoints = safeEvents
    .map((e) => ({ ...e, price: data.find((d) => d.date === e.eventDate)?.price }))
    .filter((e) => e.price != null)

  return (
    <div className="card p-5 flex flex-col gap-4">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2.5">
          <span className="material-symbols-outlined text-primary text-[22px]">insights</span>
          <h3 className="text-headline-md font-bold text-on-surface">Fiyat Geçmişi</h3>
          {dataSource && <DataSourceBadge source={dataSource} />}
        </div>
        <div className="flex gap-1">
          {RANGES.map((r) => (
            <button
              key={r.value}
              onClick={() => onRangeChange(r.value)}
              className={`px-3 py-1.5 rounded-lg text-label-md transition-colors ${
                selectedRange === r.value
                  ? 'bg-primary text-on-primary font-bold shadow-sm'
                  : 'text-on-surface-variant hover:bg-surface-container-low'
              }`}
            >
              {r.label}
            </button>
          ))}
        </div>
      </div>

      {/* Hint when events exist */}
      {eventPoints.length > 0 && (
        <p className="text-xs text-on-surface-variant bg-surface-container-low rounded-lg px-3 py-2">
          <span className="material-symbols-outlined text-[13px] align-middle mr-1">touch_app</span>
          Renkli olay noktalarına ya da herhangi bir tarihe tıklayarak fiyatın hikâyesini öğrenebilirsin.
        </p>
      )}

      {/* Chart */}
      {data.length === 0 ? (
        <div className="h-64 flex flex-col items-center justify-center gap-2 text-on-surface-variant text-body-md">
          {dataSource === 'UNAVAILABLE' ? (
            <>
              <span className="material-symbols-outlined text-orange-500 text-[28px]">cloud_off</span>
              <p className="text-on-surface font-medium">Gerçek piyasa verisi şu an mevcut değil.</p>
              <p className="text-xs text-outline">Sağlayıcı senkronizasyonu deneniyor. Birkaç saniye sonra sayfayı yenileyin.</p>
            </>
          ) : (
            <p>Fiyat verisi bulunamadı.</p>
          )}
        </div>
      ) : (
        <ResponsiveContainer width="100%" height={300}>
          <LineChart
            data={data}
            margin={{ top: 8, right: 12, left: 0, bottom: 0 }}
            onClick={(state) => {
              if (!onChartClick) return
              const payload = state?.activePayload?.[0]?.payload
              if (payload?.date && !payload.hasEvent) onChartClick(payload.date)
            }}
            style={{ cursor: onChartClick ? 'pointer' : 'default' }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke="#dde9ff" vertical={false} />
            <XAxis
              dataKey="date"
              tick={{ fontSize: 11, fill: '#6d797c' }}
              tickFormatter={(d) =>
                new Date(d).toLocaleDateString('tr-TR', { month: 'short', day: 'numeric' })
              }
              interval="preserveStartEnd"
              axisLine={false}
              tickLine={false}
            />
            <YAxis
              tick={{ fontSize: 11, fill: '#6d797c' }}
              tickFormatter={(v) => `₺${v.toFixed(0)}`}
              domain={['auto', 'auto']}
              width={58}
              axisLine={false}
              tickLine={false}
            />
            <Tooltip content={<CustomTooltip />} />
            <Line
              type="monotone"
              dataKey="price"
              stroke="#006876"
              strokeWidth={2.5}
              dot={false}
              activeDot={{ r: 5, fill: '#006876', strokeWidth: 0 }}
            />
            {eventPoints.map((e) => {
              const isSelected = selectedEvent?.id === e.id
              const fillColor =
                e.eventType === 'RISE' ? '#16a34a' :
                e.eventType === 'FALL' ? '#ba1a1a' : '#da8a36'
              return (
                <ReferenceDot
                  key={e.id}
                  x={e.eventDate}
                  y={e.price}
                  r={isSelected ? 11 : 7}
                  fill={fillColor}
                  stroke={isSelected ? '#ffd700' : '#ffffff'}
                  strokeWidth={isSelected ? 3 : 2}
                  style={{ cursor: 'pointer' }}
                  onClick={() => onEventClick?.(e)}
                />
              )
            })}
          </LineChart>
        </ResponsiveContainer>
      )}

      {/* Legend */}
      {eventPoints.length > 0 && (
        <div className="flex flex-wrap items-center gap-x-4 gap-y-1.5">
          <span className="text-xs text-outline">Olaylar:</span>
          <span className="flex items-center gap-1.5 text-xs text-positive font-medium">
            <span className="w-2.5 h-2.5 rounded-full bg-positive inline-block" />Yükseliş
          </span>
          <span className="flex items-center gap-1.5 text-xs text-error font-medium">
            <span className="w-2.5 h-2.5 rounded-full bg-error inline-block" />Düşüş
          </span>
          <span className="flex items-center gap-1.5 text-xs text-tertiary font-medium">
            <span className="w-2.5 h-2.5 rounded-full bg-tertiary-container inline-block" />Tarafsız
          </span>
        </div>
      )}

      <p className="text-xs text-outline">
        Veriler gecikmeli/gün sonu olabilir. Bu platform yatırım tavsiyesi vermez.
      </p>
    </div>
  )
}
