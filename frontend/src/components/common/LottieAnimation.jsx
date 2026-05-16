import { useEffect, useRef } from 'react'

export default function LottieAnimation({ src, className = '', loop = true, style }) {
  const containerRef = useRef(null)

  useEffect(() => {
    if (!containerRef.current || !src) return
    let anim = null
    let cancelled = false
    const controller = new AbortController()

    async function load() {
      try {
        const [{ default: lottie }, data] = await Promise.all([
          import('lottie-web'),
          fetch(src, { signal: controller.signal }).then((r) => r.json()),
        ])
        if (cancelled || !containerRef.current) return
        anim = lottie.loadAnimation({
          container: containerRef.current,
          renderer: 'svg',
          loop,
          autoplay: true,
          animationData: data,
        })
      } catch {
        // graceful fallback — container stays empty
      }
    }

    load()

    return () => {
      cancelled = true
      controller.abort()
      anim?.destroy()
    }
  }, [src, loop])

  return <div ref={containerRef} className={className} style={style} />
}
