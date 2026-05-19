// Normalize unknown API payloads into a plain JS array.
//
// Accepts:
//   raw array:        [...]
//   wrapped:          { lessons: [...] } / { stocks: [...] } / { events: [...] }
//   axios-style:      { data: [...] }
//   paginated:        { content: [...] }            (Spring Page)
//   any other shape:  -> []
//
// Usage:
//   toArray(payload)                       // tries common keys
//   toArray(payload, ['lessons'])          // tries given keys first
export function toArray(value, keys = []) {
  if (Array.isArray(value)) return value
  if (value && typeof value === 'object') {
    const candidates = [
      ...keys,
      'data',
      'content',
      'items',
      'results',
      'lessons',
      'stocks',
      'events',
      'prices',
      'news',
    ]
    for (const k of candidates) {
      if (Array.isArray(value[k])) return value[k]
    }
  }
  return []
}
