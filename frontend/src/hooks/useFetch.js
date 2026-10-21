import { useCallback, useEffect, useRef, useState } from 'react'

// Runs an async function on mount and whenever deps change.
//  - reload():  refetches quietly (keeps showing the old data meanwhile)
//  - pollMs:    optional, refetches quietly on an interval (used to watch seats change live)
export function useFetch(fn, deps = [], { pollMs } = {}) {
  const [state, setState] = useState({ data: null, loading: true, error: null })
  const [tick, setTick] = useState(0)
  const reload = useCallback(() => setTick((t) => t + 1), [])
  const lastKey = useRef(null)
  const key = JSON.stringify(deps)

  useEffect(() => {
    let cancelled = false
    const keyChanged = lastKey.current !== key
    lastKey.current = key
    setState((s) => ({
      data: keyChanged ? null : s.data,
      loading: keyChanged || s.data === null,
      error: null,
    }))
    fn()
      .then((data) => !cancelled && setState({ data, loading: false, error: null }))
      .catch((error) => !cancelled && setState({ data: null, loading: false, error }))
    return () => { cancelled = true }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key, tick])

  useEffect(() => {
    if (!pollMs) return undefined
    let cancelled = false
    const timer = setInterval(() => {
      fn().then((data) => !cancelled && setState((s) => ({ ...s, data, error: null }))).catch(() => {})
    }, pollMs)
    return () => { cancelled = true; clearInterval(timer) }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pollMs, key])

  return { ...state, reload }
}
