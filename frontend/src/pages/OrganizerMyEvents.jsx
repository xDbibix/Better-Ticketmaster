import React, { useEffect, useState } from 'react'
import api from '../api/axiosConfig'

function parsePossiblyLocalDateTime(value) {
  if (!value) return null
  if (value instanceof Date) return Number.isNaN(value.getTime()) ? null : value

  if (typeof value === 'object') {
    const year = value.year
    const month = value.monthValue ?? value.month
    const day = value.dayOfMonth ?? value.day
    const hour = value.hour ?? 0
    const minute = value.minute ?? 0
    const second = value.second ?? 0
    if (Number.isFinite(year) && Number.isFinite(month) && Number.isFinite(day)) {
      const d = new Date(year, Number(month) - 1, day, hour, minute, second)
      return Number.isNaN(d.getTime()) ? null : d
    }
  }

  if (typeof value !== 'string') return null
  const s = value.trim()
  if (!s) return null

  if (/[zZ]$/.test(s) || /[+-]\d{2}:?\d{2}$/.test(s)) {
    const d = new Date(s)
    return Number.isNaN(d.getTime()) ? null : d
  }

  const m = s.match(/^(\d{4})-(\d{2})-(\d{2})[T\s](\d{2}):(\d{2})(?::(\d{2}))?$/)
  if (m) {
    const [, yy, mm, dd, hh, mi, ss] = m
    const d = new Date(Number(yy), Number(mm) - 1, Number(dd), Number(hh), Number(mi), ss ? Number(ss) : 0)
    return Number.isNaN(d.getTime()) ? null : d
  }

  const d = new Date(s)
  return Number.isNaN(d.getTime()) ? null : d
}

function formatDateTime(value) {
  const d = parsePossiblyLocalDateTime(value)
  if (!d) return value ? String(value) : ''
  try {
    return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(d)
  } catch {
    return d.toLocaleString()
  }
}

export default function OrganizerMyEvents() {
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [me, setMe] = useState(null)

  const [myEvents, setMyEvents] = useState([])
  const [loadingEvents, setLoadingEvents] = useState(false)
  const [closingEventId, setClosingEventId] = useState('')

  const clearAlerts = () => {
    setError('')
    setMessage('')
  }

  const setErrorFromRequest = (fallback, e) => {
    const data = e?.response?.data
    if (typeof data === 'string' && data.trim()) return setError(data)
    if (data && typeof data === 'object') return setError(JSON.stringify(data))
    return setError(fallback)
  }

  const loadMe = async () => {
    try {
      const r = await api.get('/auth/me')
      setMe(r.data)
    } catch {
      setMe(null)
    }
  }

  const loadMyEvents = async () => {
    setLoadingEvents(true)
    try {
      const r = await api.get('/events?mine=true')
      const list = Array.isArray(r.data) ? r.data : []
      const filtered = list.filter(e => e && String(e?.status || '').toUpperCase() !== 'REJECTED')
      filtered.sort((a, b) => {
        const aClosed = String(a?.status || '').toUpperCase() === 'CLOSED'
        const bClosed = String(b?.status || '').toUpperCase() === 'CLOSED'
        if (aClosed !== bClosed) return aClosed ? 1 : -1
        const ad = parsePossiblyLocalDateTime(a?.dateTime)
        const bd = parsePossiblyLocalDateTime(b?.dateTime)
        const at = ad ? ad.getTime() : 0
        const bt = bd ? bd.getTime() : 0
        return bt - at
      })
      setMyEvents(filtered)
    } catch (e) {
      setMyEvents([])
      setErrorFromRequest('Could not load your events.', e)
    } finally {
      setLoadingEvents(false)
    }
  }

  const closeEvent = async (eventId) => {
    if (!eventId) return
    clearAlerts()
    setClosingEventId(eventId)
    try {
      const r = await api.post(`/events/${eventId}/close`)
      setMessage(`Event closed: ${r.data?.title || r.data?.id || eventId}`)
      await loadMyEvents()
    } catch (e) {
      setErrorFromRequest('Close event failed.', e)
    } finally {
      setClosingEventId('')
    }
  }

  useEffect(() => {
    loadMe()
  }, [])

  useEffect(() => {
    if (me?.id) loadMyEvents()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [me?.id])

  return (
    <div>
      <div className="card">
        <h2 style={{ marginTop: 0 }}>My events</h2>
        <p className="muted" style={{ marginTop: 6 }}>
          Manage and close your events.
        </p>
        {error ? <div style={{ color: 'crimson' }}>{String(error)}</div> : null}
        {message ? <div style={{ color: 'green' }}>{String(message)}</div> : null}
      </div>

      <div className="card" style={{ marginTop: 16 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8, flexWrap: 'wrap' }}>
          <div className="muted" style={{ fontSize: 12 }}>
            {me ? `Logged in as: ${me.email || me.name || me.id}` : 'Log in as an organizer to manage events.'}
          </div>
          <button className="btn" onClick={() => { clearAlerts(); loadMyEvents() }} disabled={loadingEvents || !me}>
            {loadingEvents ? 'Loading…' : 'Refresh'}
          </button>
        </div>

        {me && myEvents.length === 0 ? <div className="muted" style={{ marginTop: 8 }}>No events found for your organizer account.</div> : null}

        {myEvents.length ? (
          <div style={{ display: 'grid', gap: 8, marginTop: 10 }}>
            {myEvents.map(ev => {
              const status = String(ev?.status || '').toUpperCase()
              const isClosed = status === 'CLOSED'
              return (
                <div key={ev.id} className="card" style={{ margin: 0 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8, flexWrap: 'wrap' }}>
                    <div>
                      <div style={{ fontWeight: 700 }}>{ev.title}</div>
                      <div className="muted" style={{ fontSize: 12 }}>
                        {ev.venueName ? `${ev.venueName} • ` : ''}
                        {ev.dateTime ? formatDateTime(ev.dateTime) : ''}
                        {status ? ` • ${status}` : ''}
                      </div>
                    </div>
                    <button
                      className="btn"
                      onClick={() => closeEvent(ev.id)}
                      disabled={isClosed || closingEventId === ev.id}
                      title={isClosed ? 'This event is already closed' : 'Close this event (blocks new holds/purchases/resale)'}
                    >
                      {isClosed ? 'Closed' : (closingEventId === ev.id ? 'Closing…' : 'Close event')}
                    </button>
                  </div>
                </div>
              )
            })}
          </div>
        ) : null}
      </div>
    </div>
  )
}
