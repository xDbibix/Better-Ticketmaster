import React, { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axiosConfig'

export default function MyTickets() {
  const [tickets, setTickets] = useState([])
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [resellPriceById, setResellPriceById] = useState({})
  const [transferEmailById, setTransferEmailById] = useState({})

  const eventById = useMemo(() => {
    const map = {}
    for (const e of events) map[e.id] = e
    return map
  }, [events])

  const load = async () => {
    setLoading(true)
    setError('')
    try {
      const [tRes, eRes] = await Promise.all([api.get('/tickets/mine'), api.get('/events')])
      setTickets(Array.isArray(tRes.data) ? tRes.data : [])
      setEvents(Array.isArray(eRes.data) ? eRes.data : [])
    } catch (e) {
      setTickets([])
      setEvents([])
      setError('Could not load tickets.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const onResell = async (ticketId) => {
    setError('')
    const raw = resellPriceById[ticketId]
    const price = Number.parseFloat(raw)
    if (!Number.isFinite(price) || price <= 0) {
      setError('Enter a valid resale price.')
      return
    }
    try {
      await api.post(`/tickets/${ticketId}/resell`, { price })
      await load()
    } catch (e) {
      setError(e?.response?.data || 'Resell failed.')
    }
  }

  const onTransfer = async (ticketId) => {
    setError('')
    const toEmail = (transferEmailById[ticketId] || '').trim()
    if (!toEmail) {
      setError('Enter a recipient email.')
      return
    }
    try {
      await api.post(`/tickets/${ticketId}/transfer`, { toEmail })
      await load()
    } catch (e) {
      setError(e?.response?.data || 'Transfer failed.')
    }
  }

  if (loading) return <div>Loading…</div>

  return (
    <div>
      <div className="card">
        <h2 style={{ marginTop: 0 }}>My Tickets</h2>
        <p className="muted" style={{ marginTop: 6 }}>
          List tickets for resale or transfer them to another user.
        </p>
        {error ? <p style={{ color: 'crimson' }}>{String(error)}</p> : null}
      </div>

      {!tickets.length ? (
        <div className="card" style={{ marginTop: 16 }}>
          <p style={{ margin: 0 }}>No tickets found.</p>
          <p className="muted" style={{ margin: '6px 0 0' }}>
            Buy seats from an event page first.
          </p>
        </div>
      ) : (
        <div className="grid" style={{ marginTop: 16 }}>
          {tickets.map((t) => {
            const ev = eventById[t.eventId]
            return (
              <div className="card" key={t.id}>
                <h4 style={{ marginTop: 0 }}>{ev?.title || 'Event'}</h4>
                <div className="muted">{ev?.venueName || ''}</div>
                <div className="muted">{ev?.dateTime || ''}</div>

                <div style={{ marginTop: 8 }}>
                  <div><b>Seat:</b> {t.seatId}</div>
                  <div className="muted" style={{ fontSize: 12 }}>Ticket ID: {t.id}</div>
                </div>

                <div style={{ marginTop: 10, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                  <Link className="btn" to={`/event/${t.eventId}`}>View Event</Link>
                </div>

                <div style={{ marginTop: 12 }}>
                  {t.resale ? (
                    <div className="muted">Listed for resale at ${t.resalePrice}</div>
                  ) : (
                    <>
                      <div style={{ display: 'grid', gap: 8 }}>
                        <div>
                          <label className="muted">Resale price</label>
                          <input
                            type="number"
                            min="0"
                            step="0.01"
                            value={resellPriceById[t.id] ?? ''}
                            onChange={(e) => setResellPriceById((s) => ({ ...s, [t.id]: e.target.value }))}
                            placeholder="e.g. 25.00"
                          />
                          <button className="btn" onClick={() => onResell(t.id)} style={{ marginTop: 6 }}>
                            List for resale
                          </button>
                        </div>

                        <div>
                          <label className="muted">Transfer to (email)</label>
                          <input
                            value={transferEmailById[t.id] ?? ''}
                            onChange={(e) => setTransferEmailById((s) => ({ ...s, [t.id]: e.target.value }))}
                            placeholder="recipient@btm.test"
                          />
                          <button className="btn" onClick={() => onTransfer(t.id)} style={{ marginTop: 6 }}>
                            Transfer
                          </button>
                        </div>
                      </div>
                    </>
                  )}
                </div>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
