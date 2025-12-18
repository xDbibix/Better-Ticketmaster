import React, { useEffect, useMemo, useState } from 'react'
import api from '../api/axiosConfig'

function normalizeLocalDateTime(value) {
  const v = (value || '').trim()
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(v)) return `${v}:00`
  return v
}

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

export default function AdminDashboard() {
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [editingId, setEditingId] = useState('')
  const [editFormById, setEditFormById] = useState({})

  const sortedEvents = useMemo(() => {
    const list = Array.isArray(events) ? [...events] : []
    // Admin tab should not show CLOSED events.
    const filtered = list.filter((ev) => String(ev?.status || '').trim().toUpperCase() !== 'CLOSED')
    filtered.sort((a, b) => String(a?.title || '').localeCompare(String(b?.title || '')))
    return filtered
  }, [events])

  const load = async () => {
    setLoading(true)
    setError('')
    setMessage('')
    try {
      const r = await api.get('/admin/events')
      setEvents(Array.isArray(r.data) ? r.data : [])
    } catch (e) {
      setEvents([])
      setError(e?.response?.data || 'Could not load admin events.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const resetTickets = async () => {
    setError('')
    setMessage('')
    if (!confirm('Reset ALL tickets/resales for ALL users? This also resets seats to AVAILABLE.')) return
    try {
      const r = await api.post('/admin/reset/tickets')
      setMessage(`Reset complete: ${JSON.stringify(r.data)}`)
      await load()
    } catch (e) {
      setError(e?.response?.data || 'Reset tickets failed.')
    }
  }

  const resetResalesOnly = async () => {
    setError('')
    setMessage('')
    if (!confirm('Clear ALL resale listings (keep ticket ownership)?')) return
    try {
      const r = await api.post('/admin/reset/resales')
      setMessage(`Resales cleared: ${JSON.stringify(r.data)}`)
      await load()
    } catch (e) {
      setError(e?.response?.data || 'Reset resales failed.')
    }
  }

  const takedownEvent = async (eventId) => {
    setError('')
    setMessage('')
    if (!confirm('Take down (close) this event?')) return
    try {
      await api.post(`/events/${eventId}/close`)
      setMessage('Event closed.')
      await load()
    } catch (e) {
      setError(e?.response?.data || 'Close event failed.')
    }
  }

  const startEdit = (ev) => {
    setEditingId(ev?.id || '')
    setEditFormById((s) => ({
      ...s,
      [ev.id]: {
        title: ev?.title || '',
        venueName: ev?.venueName || '',
        dateTime: '',
        minResale: ev?.minResale ?? 0,
        maxResale: ev?.maxResale ?? 0,
        description: ev?.description || '',
        imageUrl: ev?.imageUrl || '',
      },
    }))
  }

  const saveEdit = async (eventId) => {
    setError('')
    setMessage('')
    const form = editFormById[eventId]
    if (!form) return

    try {
      const body = {
        title: form.title,
        venueName: form.venueName,
        dateTime: form.dateTime ? normalizeLocalDateTime(form.dateTime) : undefined,
        minResale: Number(form.minResale),
        maxResale: Number(form.maxResale),
        description: form.description,
        imageUrl: form.imageUrl,
      }
      await api.post(`/admin/events/${eventId}/update`, body)
      setMessage('Event updated.')
      setEditingId('')
      await load()
    } catch (e) {
      setError(e?.response?.data || 'Update event failed.')
    }
  }

  if (loading) return <div>Loading…</div>

  return (
    <div>
      <div className="card">
        <h2 style={{ marginTop: 0 }}>Admin Dashboard</h2>
        <p className="muted" style={{ marginTop: 6 }}>
          Reset tickets/resales and manage events.
        </p>
        {error ? <div style={{ color: 'crimson' }}>{String(error)}</div> : null}
        {message ? <div style={{ color: 'green' }}>{String(message)}</div> : null}

        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 12 }}>
          <button className="btn" onClick={resetResalesOnly}>Reset resales</button>
          <button className="btn" onClick={resetTickets}>Reset tickets + resales</button>
          <button className="btn" onClick={load}>Refresh</button>
        </div>
      </div>

      <div className="card" style={{ marginTop: 16 }}>
        <h3 style={{ marginTop: 0 }}>All events</h3>
        <div className="muted" style={{ fontSize: 12, marginTop: -6 }}>
          Close events to take them down from the public list. Use Edit to change details.
        </div>
      </div>

      {!sortedEvents.length ? (
        <div className="card" style={{ marginTop: 16 }}>
          <div className="muted">No events found.</div>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: 12, marginTop: 16 }}>
          {sortedEvents.map((ev) => {
            const isClosed = String(ev?.status || '').toUpperCase() === 'CLOSED'
            const isEditing = editingId === ev.id
            const form = editFormById[ev.id]
            return (
              <div className="card" key={ev.id}>
                <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12, flexWrap: 'wrap' }}>
                  <div>
                    <h4 style={{ marginTop: 0, marginBottom: 4 }}>{ev.title || 'Untitled event'}</h4>
                    <div className="muted">{ev.venueName || ''}</div>
                    <div className="muted">{formatDateTime(ev.dateTime)}</div>
                    <div className="muted">Status: {ev.status || '—'}</div>
                  </div>

                  <div style={{ display: 'flex', gap: 8, alignItems: 'flex-start', flexWrap: 'wrap' }}>
                    <button className="btn" onClick={() => startEdit(ev)}>Edit</button>
                    <button className="btn" onClick={() => takedownEvent(ev.id)} disabled={isClosed} title={isClosed ? 'Already closed' : 'Close (take down)'}>
                      Take down
                    </button>
                  </div>
                </div>

                {isEditing && form ? (
                  <div style={{ marginTop: 12 }}>
                    <div className="form" style={{ margin: 0 }}>
                      <label className="muted">Title</label>
                      <input value={form.title} onChange={(e) => setEditFormById((s) => ({ ...s, [ev.id]: { ...s[ev.id], title: e.target.value } }))} />

                      <label className="muted">Venue name</label>
                      <input value={form.venueName} onChange={(e) => setEditFormById((s) => ({ ...s, [ev.id]: { ...s[ev.id], venueName: e.target.value } }))} />

                      <label className="muted">Date/time (optional)</label>
                      <input type="datetime-local" value={form.dateTime} onChange={(e) => setEditFormById((s) => ({ ...s, [ev.id]: { ...s[ev.id], dateTime: e.target.value } }))} />
                      <div className="muted" style={{ fontSize: 12, marginTop: 4 }}>
                        Leave blank to keep current: {formatDateTime(ev.dateTime)}
                      </div>

                      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 10 }}>
                        <div style={{ flex: '1 1 160px' }}>
                          <label className="muted">Min resale</label>
                          <input type="number" value={form.minResale} onChange={(e) => setEditFormById((s) => ({ ...s, [ev.id]: { ...s[ev.id], minResale: e.target.value } }))} />
                        </div>
                        <div style={{ flex: '1 1 160px' }}>
                          <label className="muted">Max resale</label>
                          <input type="number" value={form.maxResale} onChange={(e) => setEditFormById((s) => ({ ...s, [ev.id]: { ...s[ev.id], maxResale: e.target.value } }))} />
                        </div>
                      </div>

                      <label className="muted" style={{ marginTop: 10 }}>Description</label>
                      <textarea rows={3} value={form.description} onChange={(e) => setEditFormById((s) => ({ ...s, [ev.id]: { ...s[ev.id], description: e.target.value } }))} />

                      <label className="muted">Image URL</label>
                      <input value={form.imageUrl} onChange={(e) => setEditFormById((s) => ({ ...s, [ev.id]: { ...s[ev.id], imageUrl: e.target.value } }))} />

                      <div style={{ display: 'flex', gap: 8, marginTop: 12, flexWrap: 'wrap' }}>
                        <button className="btn" onClick={() => saveEdit(ev.id)}>Save</button>
                        <button className="btn" onClick={() => setEditingId('')}>Cancel</button>
                      </div>
                    </div>
                  </div>
                ) : null}
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
