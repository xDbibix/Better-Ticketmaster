import React, { useContext, useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import api from '../api/axiosConfig'
import { AuthContext } from '../context/AuthContext'
import SeatMap from '../components/SeatMap'

export default function Event() {
  const { id } = useParams()
  const HOLD_SECONDS = 5 * 60
  const [event, setEvent] = useState(null)
  const [selectedSeats, setSelectedSeats] = useState([])
  const [holdTimeLeft, setHoldTimeLeft] = useState(0)
  const [isHolding, setIsHolding] = useState(false)
  const [resaleTickets, setResaleTickets] = useState([])
  const [resaleLoading, setResaleLoading] = useState(true)
  const [resaleError, setResaleError] = useState('')
  const { user } = useContext(AuthContext)
  const seatMapRef = useRef()
  const holdTimerRef = useRef(null)
  const holdAbortRef = useRef(null)
  const holdDebounceRef = useRef(null)
  const lastHeldSeatIdsRef = useRef([])

  useEffect(() => {
    api.get(`/events/${id}`).then(r => setEvent(r.data)).catch(() => setEvent(null))
  }, [id])

  const loadResale = () => {
    if (!id) return
    setResaleLoading(true)
    setResaleError('')
    api
      .get(`/tickets/resale?eventId=${id}`)
      .then(r => setResaleTickets(Array.isArray(r.data) ? r.data : []))
      .catch(() => {
        setResaleTickets([])
        setResaleError('Could not load resale tickets.')
      })
      .finally(() => setResaleLoading(false))
  }

  useEffect(() => {
    loadResale()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  useEffect(() => {
    // Cancel any pending debounce.
    if (holdDebounceRef.current) {
      clearTimeout(holdDebounceRef.current)
      holdDebounceRef.current = null
    }

    // Abort any in-flight hold request.
    if (holdAbortRef.current) {
      try { holdAbortRef.current.abort() } catch {}
      holdAbortRef.current = null
    }

    if (holdTimerRef.current) {
      clearInterval(holdTimerRef.current)
      holdTimerRef.current = null
    }

    if (selectedSeats.length === 0) {
      setHoldTimeLeft(0)
      // Best-effort: release anything we previously held.
      const toRelease = Array.isArray(lastHeldSeatIdsRef.current) ? lastHeldSeatIdsRef.current : []
      lastHeldSeatIdsRef.current = []
      if (toRelease.length) {
        api.post('/seats/release', { seatIds: toRelease }).catch(() => {})
      }
      return
    }

    // Debounce holds so multiple clicks (multi-select) result in a single hold attempt.
    holdDebounceRef.current = setTimeout(() => {
      const seatIds = selectedSeats
      const ctrl = new AbortController()
      holdAbortRef.current = ctrl
      setIsHolding(true)

      api
        .post('/seats/hold', { seatIds }, { signal: ctrl.signal })
        .then(() => {
          lastHeldSeatIdsRef.current = seatIds
          setHoldTimeLeft(HOLD_SECONDS)
          holdTimerRef.current = setInterval(() => {
            setHoldTimeLeft(prev => {
              if (prev <= 1) {
                if (holdTimerRef.current) clearInterval(holdTimerRef.current)
                holdTimerRef.current = null
                const toRelease = Array.isArray(lastHeldSeatIdsRef.current) ? lastHeldSeatIdsRef.current : []
                lastHeldSeatIdsRef.current = []
                setSelectedSeats([])
                if (toRelease.length) api.post('/seats/release', { seatIds: toRelease }).catch(() => {})
                alert('Seat hold expired. Please reselect your seats.')
                if (seatMapRef.current?.reload) seatMapRef.current.reload()
                return 0
              }
              return prev - 1
            })
          }, 1000)
        })
        .catch((err) => {
          // Ignore aborts from rapid selection changes.
          if (err?.name === 'CanceledError' || err?.code === 'ERR_CANCELED') return

          alert(err?.response?.data || 'Could not hold selected seats. They may have been taken.')

          // Release any seats from this attempted selection (and anything we previously held)
          // so nothing gets stuck grey without being purchased.
          const prior = Array.isArray(lastHeldSeatIdsRef.current) ? lastHeldSeatIdsRef.current : []
          lastHeldSeatIdsRef.current = []
          const toRelease = Array.from(new Set([...(prior || []), ...(seatIds || [])]))
          if (toRelease.length) api.post('/seats/release', { seatIds: toRelease }).catch(() => {})

          setSelectedSeats([])
          if (seatMapRef.current?.reload) seatMapRef.current.reload()
        })
        .finally(() => {
          setIsHolding(false)
          holdAbortRef.current = null
        })
    }, 250)

    return () => {
      if (holdDebounceRef.current) {
        clearTimeout(holdDebounceRef.current)
        holdDebounceRef.current = null
      }
      if (holdAbortRef.current) {
        try { holdAbortRef.current.abort() } catch {}
        holdAbortRef.current = null
      }
    }
  }, [selectedSeats])

  const handleBuy = async () => {
    if (!selectedSeats.length) return alert('Select at least one seat.')
    if (holdTimeLeft <= 0) return alert('Please wait for seats to be held before buying.')
    try {
      const booking = { eventId: id, seatIds: selectedSeats, totalPrice: 10.0 * selectedSeats.length }
      const r = await api.post('/bookings', booking)
      await api.post(`/bookings/${r.data.id}/complete`)
      alert('Purchase complete — confirmation sent (or logged)')
      setSelectedSeats([])
      if (seatMapRef.current?.reload) seatMapRef.current.reload()
    } catch {
      alert('Purchase failed')
    }
  }

  const handleBuyResale = async (ticketId) => {
    if (!user) return
    try {
      await api.post(`/tickets/${ticketId}/buy`)
      alert('Resale purchase complete')
      loadResale()
      if (seatMapRef.current?.reload) seatMapRef.current.reload()
    } catch (e) {
      alert(e?.response?.data || 'Resale purchase failed')
    }
  }

  if (!event) return <div>Loading...</div>

  return (
    <div>
      <div className="card">
        <h2>{event.title || event.name}</h2>
        <p className="muted">
          {event.venueName || 'Unknown Venue'} — {event.dateTime || event.date}
        </p>
        <p>{event.description}</p>

        {holdTimeLeft > 0 && (
          <div style={{ color: 'orange', marginBottom: 8 }}>
            Seats held for: {Math.floor(holdTimeLeft / 60)}:{(holdTimeLeft % 60).toString().padStart(2, '0')}
          </div>
        )}

        <SeatMap ref={seatMapRef} eventId={id} selectedSeats={selectedSeats} setSelectedSeats={setSelectedSeats} />

        <div style={{ display: 'flex', gap: 8, marginTop: 16 }}>
          {user ? (
            <button className="btn" onClick={handleBuy} disabled={!selectedSeats.length || isHolding || holdTimeLeft <= 0}>
              Buy Selected Seats
            </button>
          ) : (
            <Link to="/login" className="btn">
              Sign in to buy
            </Link>
          )}
        </div>
      </div>

      <div className="card" style={{ marginTop: 16 }}>
        <h3 style={{ marginTop: 0 }}>Resale tickets</h3>
        <p className="muted" style={{ marginTop: 6 }}>
          Organizer resale range: ${event.minResale}–${event.maxResale}
        </p>

        {resaleLoading ? <div>Loading…</div> : null}
        {!resaleLoading && resaleError ? <div style={{ color: 'crimson' }}>{resaleError}</div> : null}

        {!resaleLoading && !resaleError && resaleTickets.length === 0 ? (
          <div className="muted">No resale tickets listed for this event.</div>
        ) : null}

        {!resaleLoading && !resaleError && resaleTickets.length > 0 ? (
          <div style={{ display: 'grid', gap: 10, marginTop: 10 }}>
            {resaleTickets.map((t) => (
              <div key={t.id} style={{ display: 'flex', justifyContent: 'space-between', gap: 12, alignItems: 'center' }}>
                <div>
                  <div><b>${t.resalePrice}</b></div>
                  <div className="muted" style={{ fontSize: 12 }}>Seat: {t.seatId}</div>
                </div>

                {user ? (
                  <button className="btn" onClick={() => handleBuyResale(t.id)}>
                    Buy resale
                  </button>
                ) : (
                  <Link className="btn" to="/login">Sign in to buy</Link>
                )}
              </div>
            ))}
          </div>
        ) : null}
      </div>
    </div>
  )
}
