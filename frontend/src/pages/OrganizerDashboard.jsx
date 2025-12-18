import React, { useEffect, useMemo, useState } from 'react'
import api from '../api/axiosConfig'

const VENUE_TYPES = ['ARENA', 'STADIUM', 'THEATRE', 'CLUB', 'OUTDOOR', 'OTHER']
const SECTION_TYPES = ['SEATED', 'GA']

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

  // If timezone is included, let Date parse it.
  if (/[zZ]$/.test(s) || /[+-]\d{2}:?\d{2}$/.test(s)) {
    const d = new Date(s)
    return Number.isNaN(d.getTime()) ? null : d
  }

  // Treat plain LocalDateTime (no timezone) as local time.
  const m = s.match(/^(\d{4})-(\d{2})-(\d{2})[T\s](\d{2}):(\d{2})(?::(\d{2}))?$/)
  if (m) {
    const [, yy, mm, dd, hh, mi, ss] = m
    const d = new Date(
      Number(yy),
      Number(mm) - 1,
      Number(dd),
      Number(hh),
      Number(mi),
      ss ? Number(ss) : 0
    )
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

function parseRows(text) {
  const raw = (text || '').split(',').map(s => s.trim()).filter(Boolean)
  return raw.length ? raw : ['A', 'B', 'C']
}

function normalizeLocalDateTime(value) {
  const v = (value || '').trim()
  // datetime-local commonly returns YYYY-MM-DDTHH:mm (no seconds). Spring's LocalDateTime parsing can be strict,
  // so normalize to YYYY-MM-DDTHH:mm:ss.
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(v)) return `${v}:00`
  return v
}

function toFiniteNumber(value, fallback = 0) {
  const n = Number(value)
  return Number.isFinite(n) ? n : fallback
}

function LayoutSeatPreview({ layoutView, draftSection }) {
  if (!layoutView) return null
  const cw = Math.max(1, toFiniteNumber(layoutView.canvasWidth, 1200))
  const ch = Math.max(1, toFiniteNumber(layoutView.canvasHeight, 800))
  const sections = Array.isArray(layoutView.sections) ? layoutView.sections : []
  const allSections = draftSection ? [...sections, { ...draftSection, __draft: true }] : sections

  // Match the existing SeatMap colors (no orange in preview).
  const COLOR_AVAILABLE = '#0b57d0'
  const COLOR_UNAVAILABLE = '#aaa'

  const [pan, setPan] = useState({ x: 0, y: 0 })
  const [drag, setDrag] = useState(null)

  // Always start centered for a given layout, but allow the user to pan afterwards.
  useEffect(() => {
    setPan({ x: 0, y: 0 })
    setDrag(null)
  }, [layoutView.layoutId])

  const onPointerDown = (e) => {
    // Only pan with primary button.
    if (e.button != null && e.button !== 0) return
    e.currentTarget.setPointerCapture?.(e.pointerId)
    setDrag({
      startX: e.clientX,
      startY: e.clientY,
      baseX: pan.x,
      baseY: pan.y,
    })
  }

  const onPointerMove = (e) => {
    if (!drag) return
    const dx = e.clientX - drag.startX
    const dy = e.clientY - drag.startY
    setPan({ x: drag.baseX + dx, y: drag.baseY + dy })
  }

  const endDrag = () => setDrag(null)

  return (
    <div
      style={{ width: '100%', height: '100%', overflow: 'hidden', display: 'flex', justifyContent: 'center', alignItems: 'center' }}
      onPointerDown={onPointerDown}
      onPointerMove={onPointerMove}
      onPointerUp={endDrag}
      onPointerCancel={endDrag}
      title="Drag to move the preview"
    >
      <svg
        viewBox={`0 0 ${cw} ${ch}`}
        preserveAspectRatio="xMidYMid meet"
        style={{
          width: '100%',
          height: '100%',
          display: 'block',
          borderRadius: 8,
          border: '1px solid #e2e8f0',
          background: '#fff',
          transform: `translate(${pan.x}px, ${pan.y}px)`,
          cursor: drag ? 'grabbing' : 'grab',
          touchAction: 'none',
        }}
      >
        {layoutView.backgroundImageUrl ? (
          <image href={layoutView.backgroundImageUrl} x="0" y="0" width={cw} height={ch} opacity="0.25" preserveAspectRatio="none" />
        ) : null}

        {/* Canvas boundary */}
        <rect x="0" y="0" width={cw} height={ch} fill="transparent" stroke="#e2e8f0" />

        {allSections.map((s) => {
          const x = toFiniteNumber(s?.x, 0)
          const y = toFiniteNumber(s?.y, 0)
          const w = Math.max(1, toFiniteNumber(s?.width, 1))
          const h = Math.max(1, toFiniteNumber(s?.height, 1))
          const rows = Array.isArray(s?.rows) && s.rows.length ? s.rows : []
          const seatsPerRow = Math.max(0, Math.floor(toFiniteNumber(s?.seatsPerRow, 0)))
          const disabled = new Set(Array.isArray(s?.disabledSeats) ? s.disabledSeats : (s?.disabledSeats ? Array.from(s.disabledSeats) : []))

          const cols = seatsPerRow
          const rcount = rows.length
          // Seat spacing inside the section box.
          const dx = cols > 0 ? w / (cols + 1) : 0
          const dy = rcount > 0 ? h / (rcount + 1) : 0
          const seatR = Math.max(1.5, Math.min(5, Math.min(dx, dy) * 0.25 || 2.5))

          return (
            <g
              key={s.id || (s.__draft ? '__draft__' : `${s.sectionName}-${x}-${y}`)}
              transform={s?.rotation ? `rotate(${toFiniteNumber(s.rotation, 0)} ${x + w / 2} ${y + h / 2})` : undefined}
            >
              <rect
                x={x}
                y={y}
                width={w}
                height={h}
                fill="transparent"
                stroke={COLOR_AVAILABLE}
                strokeWidth={s.__draft ? '3' : '2'}
                strokeDasharray={s.__draft ? '8 6' : undefined}
                opacity={s.__draft ? 0.6 : 1}
              />

              {/* Seats */}
              {rows.map((rowLabel, rIdx) => {
                if (!cols) return null
                return Array.from({ length: cols }).map((_, cIdx) => {
                  const seatNum = cIdx + 1
                  const key = `${rowLabel}-${seatNum}`
                  const isDisabled = disabled.has(key)
                  const cx = x + dx * (cIdx + 1)
                  const cy = y + dy * (rIdx + 1)
                  return (
                    <circle
                      key={key}
                      cx={cx}
                      cy={cy}
                      r={seatR}
                      fill={isDisabled ? COLOR_UNAVAILABLE : COLOR_AVAILABLE}
                      opacity={isDisabled ? 0.25 : (s.__draft ? 0.55 : 0.85)}
                    />
                  )
                })
              })}
            </g>
          )
        })}
      </svg>
    </div>
  )
}

export default function OrganizerDashboard() {
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [creatingEvent, setCreatingEvent] = useState(false)

  const [venues, setVenues] = useState([])
  const [selectedVenueId, setSelectedVenueId] = useState('')
  const [selectedVenueName, setSelectedVenueName] = useState('')
  const [layouts, setLayouts] = useState([])
  const [selectedLayoutId, setSelectedLayoutId] = useState('')
  const [selectedLayoutName, setSelectedLayoutName] = useState('')
  const [layoutView, setLayoutView] = useState(null)

  const selectedVenue = useMemo(() => venues.find(v => v.id === selectedVenueId) || null, [venues, selectedVenueId])
  const selectedLayout = useMemo(() => layouts.find(l => l.id === selectedLayoutId) || null, [layouts, selectedLayoutId])

  const venueNameOptions = useMemo(() => {
    const names = new Set()
    for (const v of venues) {
      const name = (v?.venueName || '').trim()
      if (name) names.add(name)
    }
    return Array.from(names).sort((a, b) => a.localeCompare(b))
  }, [venues])

  const layoutNameOptions = useMemo(() => {
    const names = new Set()
    for (const l of layouts) {
      const name = (l?.layoutName || '').trim()
      if (name) names.add(name)
    }
    return Array.from(names).sort((a, b) => a.localeCompare(b))
  }, [layouts])

  const [venueForm, setVenueForm] = useState({
    name: '',
    location: '',
    type: 'ARENA',
  })

  const [layoutForm, setLayoutForm] = useState({
    layoutName: '',
    imageUrl: '',
  })

  const [sectionForm, setSectionForm] = useState({
    sectionName: 'Lower Bowl',
    sectionType: 'SEATED',
    x: 100,
    y: 100,
    width: 300,
    height: 200,
    rotation: 0,
    curved: false,
    radius: 0,
    arc: 0,
    rowsCsv: 'A,B,C',
    seatsPerRow: 10,
  })

  const [eventForm, setEventForm] = useState({
    title: '',
    dateTime: '',
    minResale: 5,
    maxResale: 200,
    description: '',
    imageUrl: '',
  })

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

  const loadVenues = async () => {
    try {
      const r = await api.get('/venuebuilder/venues')
      const list = Array.isArray(r.data) ? r.data : []
      setVenues(list)
      // Make the page usable immediately: if nothing selected yet, pick the first venue.
      if (!selectedVenueId && !selectedVenueName && list.length) {
        setSelectedVenueId(list[0].id)
        setSelectedVenueName(list[0].venueName || '')
      }
    } catch (e) {
      setVenues([])
      setErrorFromRequest('Could not load venues.', e)
    }
  }

  const loadLayouts = async (venueId) => {
    if (!venueId) {
      setLayouts([])
      return
    }
    try {
      const r = await api.get(`/venuebuilder/venues/${venueId}/layouts`)
      const list = Array.isArray(r.data) ? r.data : []
      setLayouts(list)

      // If nothing selected yet, pick the first layout to keep the flow smooth.
      if (!selectedLayoutId && !selectedLayoutName && list.length) {
        setSelectedLayoutId(list[0].id)
        setSelectedLayoutName(list[0].layoutName || '')
      }
    } catch (e) {
      setLayouts([])
      setErrorFromRequest('Could not load layouts.', e)
    }
  }

  const loadLayoutView = async (layoutId) => {
    if (!layoutId) {
      setLayoutView(null)
      return
    }
    try {
      const r = await api.get(`/venuebuilder/layouts/${layoutId}`)
      setLayoutView(r.data)
    } catch (e) {
      setLayoutView(null)
      setErrorFromRequest('Could not load layout preview.', e)
    }
  }

  useEffect(() => {
    loadVenues()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // My events moved to /organizer/events

  useEffect(() => {
    // Keep the displayed name in sync when a venueId is chosen programmatically.
    if (!selectedVenueId) {
      setSelectedVenueName('')
      return
    }
    const v = venues.find(x => x.id === selectedVenueId)
    if (v?.venueName) setSelectedVenueName(v.venueName)
  }, [selectedVenueId, venues])

  useEffect(() => {
    setSelectedLayoutId('')
    setSelectedLayoutName('')
    setLayoutView(null)
    loadLayouts(selectedVenueId)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedVenueId])

  useEffect(() => {
    // Keep the displayed name in sync when a layoutId is chosen programmatically.
    if (!selectedLayoutId) {
      setSelectedLayoutName('')
      return
    }
    const l = layouts.find(x => x.id === selectedLayoutId)
    if (l?.layoutName) setSelectedLayoutName(l.layoutName)
  }, [selectedLayoutId, layouts])

  useEffect(() => {
    // Keep preview in sync with selected layout for event creation.
    if (selectedLayoutId) loadLayoutView(selectedLayoutId)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedLayoutId])

  const createVenue = async () => {
    clearAlerts()
    if (!venueForm.name.trim()) return setError('Venue name is required.')
    try {
      const body = {
        name: venueForm.name.trim(),
        location: venueForm.location.trim(),
        type: venueForm.type,
      }
      const r = await api.post('/venuebuilder/venues', body)
      setMessage(`Venue created: ${r.data?.venueName || r.data?.id}`)
      setVenueForm(s => ({ ...s, name: '' }))
      await loadVenues()
      setSelectedVenueId(r.data?.id || '')
    } catch (e) {
      setErrorFromRequest('Create venue failed.', e)
    }
  }

  const createLayout = async () => {
    clearAlerts()
    if (!selectedVenueId) return setError('Select a venue first.')
    if (!layoutForm.layoutName.trim()) return setError('Layout name is required.')
    try {
      const body = {
        venueId: selectedVenueId,
        layoutName: layoutForm.layoutName.trim(),
        imageUrl: layoutForm.imageUrl.trim(),
      }
      const r = await api.post('/venuebuilder/layouts', body)
      setMessage(`Layout created: ${r.data?.layoutName || r.data?.id}`)
      setLayoutForm({ layoutName: '', imageUrl: '' })
      await loadLayouts(selectedVenueId)
      setSelectedLayoutId(r.data?.id || '')
    } catch (e) {
      setErrorFromRequest('Create layout failed.', e)
    }
  }

  const addSection = async () => {
    clearAlerts()
    if (!selectedLayoutId) return setError('Select a layout first.')
    if (!sectionForm.sectionName.trim()) return setError('Section name is required.')
    const seatsPerRow = Number(sectionForm.seatsPerRow)
    if (!Number.isFinite(seatsPerRow) || seatsPerRow <= 0) return setError('seatsPerRow must be > 0')

    try {
      const body = {
        sectionName: sectionForm.sectionName.trim(),
        sectionType: sectionForm.sectionType,
        x: Number(sectionForm.x) || 0,
        y: Number(sectionForm.y) || 0,
        width: Number(sectionForm.width) || 200,
        height: Number(sectionForm.height) || 150,
        rotation: Number(sectionForm.rotation) || 0,
        curved: !!sectionForm.curved,
        radius: Number(sectionForm.radius) || 0,
        arc: Number(sectionForm.arc) || 0,
        rows: parseRows(sectionForm.rowsCsv),
        seatsPerRow,
        capacity: parseRows(sectionForm.rowsCsv).length * seatsPerRow,
      }
      const r = await api.post(`/venuebuilder/layouts/${selectedLayoutId}/sections`, body)
      setMessage(`Section added: ${r.data?.sectionName || r.data?.id}`)
      await loadLayoutView(selectedLayoutId)
    } catch (e) {
      setErrorFromRequest('Add section failed.', e)
    }
  }

  const draftSectionForPreview = useMemo(() => {
    if (!selectedLayoutId) return null
    const rows = parseRows(sectionForm.rowsCsv)
    const seatsPerRow = Math.max(0, Math.floor(Number(sectionForm.seatsPerRow) || 0))
    return {
      id: '__draft__',
      layoutId: selectedLayoutId,
      sectionName: sectionForm.sectionName,
      sectionType: sectionForm.sectionType,
      x: Number(sectionForm.x) || 0,
      y: Number(sectionForm.y) || 0,
      width: Number(sectionForm.width) || 200,
      height: Number(sectionForm.height) || 150,
      rotation: Number(sectionForm.rotation) || 0,
      curved: !!sectionForm.curved,
      radius: Number(sectionForm.radius) || 0,
      arc: Number(sectionForm.arc) || 0,
      rows,
      seatsPerRow,
      disabledSeats: [],
    }
  }, [sectionForm, selectedLayoutId])

  const createEvent = async () => {
    clearAlerts()
    if (!selectedLayoutId) return setError('Select a layout first (event needs layoutId).')
    if (!eventForm.title.trim()) return setError('Event title is required.')
    if (!eventForm.dateTime) return setError('Event date/time is required.')

    const dateTime = normalizeLocalDateTime(eventForm.dateTime)
    if (!dateTime) return setError('Event date/time is required.')

    const minResale = Number(eventForm.minResale)
    const maxResale = Number(eventForm.maxResale)
    if (!Number.isFinite(minResale) || !Number.isFinite(maxResale) || minResale < 0 || maxResale < minResale) {
      return setError('Resale range invalid (min <= max).')
    }

    try {
      setCreatingEvent(true)
      const body = {
        layoutId: selectedLayoutId,
        title: eventForm.title.trim(),
        venueName: selectedVenue?.venueName || '',
        dateTime,
        minResale,
        maxResale,
        description: eventForm.description,
        imageUrl: eventForm.imageUrl,
      }
      const r = await api.post('/events', body)
      setMessage(`Event created: ${r.data?.title || r.data?.id} (status: ${r.data?.status || 'PENDING'})`)
      setEventForm({ title: '', dateTime: '', minResale: 5, maxResale: 200, description: '', imageUrl: '' })
    } catch (e) {
      setErrorFromRequest('Create event failed.', e)
    } finally {
      setCreatingEvent(false)
    }
  }

  const onPickEventImage = async (file) => {
    clearAlerts()
    if (!file) return
    if (!file.type?.startsWith('image/')) {
      setError('Please choose an image file.')
      return
    }
    // Store as data URL in imageUrl for demo purposes.
    // Note: large images could exceed MongoDB document size; keep files small.
    const reader = new FileReader()
    reader.onerror = () => setError('Could not read image file.')
    reader.onload = () => {
      const dataUrl = typeof reader.result === 'string' ? reader.result : ''
      if (!dataUrl) {
        setError('Could not read image file.')
        return
      }
      setEventForm(s => ({ ...s, imageUrl: dataUrl }))
      setMessage('Image attached to event.')
    }
    reader.readAsDataURL(file)
  }

  return (
    <div>
      <div className="card">
        <h2 style={{ marginTop: 0 }}>Organizer Dashboard</h2>
        <p className="muted" style={{ marginTop: 6 }}>
          Manage your events, build venues/layouts, and publish events.
        </p>
        {error ? <div style={{ color: 'crimson' }}>{String(error)}</div> : null}
        {message ? <div style={{ color: 'green' }}>{String(message)}</div> : null}
      </div>

      {/* 6 sections (cards) like before */}
      <div
        style={{
          marginTop: 16,
          display: 'grid',
          gap: 16,
          // Use 3 column stacks to avoid grid-row stretching gaps.
          gridTemplateColumns: 'minmax(280px, 1fr) minmax(420px, 1.4fr) minmax(280px, 1fr)',
          alignItems: 'start',
        }}
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div className="card">
            <h3 style={{ marginTop: 0 }}>Create venue</h3>
            <div className="muted" style={{ fontSize: 12, marginTop: -6, marginBottom: 8 }}>
              Step 1: enter venue details (name, location, type).
            </div>
            <div className="form" style={{ margin: 0 }}>
              <label className="muted">Venue name</label>
              <input value={venueForm.name} onChange={(e) => setVenueForm(s => ({ ...s, name: e.target.value }))} placeholder="Venue name" />
              <label className="muted">Location</label>
              <input value={venueForm.location} onChange={(e) => setVenueForm(s => ({ ...s, location: e.target.value }))} placeholder="Location" />
              <label className="muted">Venue type</label>
              <select value={venueForm.type} onChange={(e) => setVenueForm(s => ({ ...s, type: e.target.value }))}>
                {VENUE_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
              </select>
              <button className="btn" onClick={createVenue}>Create venue</button>
            </div>
          </div>

          <div className="card">
            <h3 style={{ marginTop: 0 }}>Create layout</h3>
            <div className="muted" style={{ fontSize: 12, marginTop: -6, marginBottom: 8 }}>
              Step 2: create a new layout for the selected venue.
            </div>
            <div className="form" style={{ margin: 0 }}>
              <label className="muted">Layout name</label>
              <input value={layoutForm.layoutName} onChange={(e) => setLayoutForm(s => ({ ...s, layoutName: e.target.value }))} placeholder="Layout name" />
              <label className="muted">Background image URL (optional)</label>
              <input value={layoutForm.imageUrl} onChange={(e) => setLayoutForm(s => ({ ...s, imageUrl: e.target.value }))} placeholder="Background image URL (optional)" />
              <button className="btn" onClick={createLayout} disabled={!selectedVenueId}>Create layout</button>
            </div>
          </div>

          <div className="card">
            <h3 style={{ marginTop: 0 }}>Select venue & layout</h3>
            <div className="muted" style={{ fontSize: 12, marginTop: -6, marginBottom: 8 }}>
              Step 3: pick an existing venue and layout.
            </div>

            <label className="muted">Venue (type or pick an existing one)</label>
            <input
              list="venue-names"
              value={selectedVenueName}
              onChange={(e) => {
                const name = e.target.value
                setSelectedVenueName(name)
                const needle = name.trim().toLowerCase()
                const match = venues.find(v => (v?.venueName || '').trim().toLowerCase() === needle)
                setSelectedVenueId(match?.id || '')
              }}
              placeholder="Start typing a venue name…"
            />
            <datalist id="venue-names">
              {venueNameOptions.map(name => <option key={name} value={name} />)}
            </datalist>

            <label className="muted" style={{ marginTop: 10 }}>Layout (type or pick an existing one)</label>
            <input
              list="layout-names"
              value={selectedLayoutName}
              onChange={(e) => {
                const name = e.target.value
                setSelectedLayoutName(name)
                const needle = name.trim().toLowerCase()
                const match = layouts.find(l => (l?.layoutName || '').trim().toLowerCase() === needle)
                setSelectedLayoutId(match?.id || '')
              }}
              placeholder={selectedVenueId ? 'Start typing a layout name…' : 'Select a venue first'}
              disabled={!selectedVenueId}
            />
            <datalist id="layout-names">
              {layoutNameOptions.map(name => <option key={name} value={name} />)}
            </datalist>

            <div style={{ display: 'flex', gap: 8, marginTop: 8, flexWrap: 'wrap' }}>
              <button className="btn" onClick={() => { clearAlerts(); loadVenues() }}>Refresh venues</button>
              <button className="btn" onClick={() => { clearAlerts(); loadLayouts(selectedVenueId) }} disabled={!selectedVenueId}>Refresh layouts</button>
            </div>

            <div className="muted" style={{ marginTop: 10, fontSize: 12 }}>
              Selected venue: {selectedVenue ? `${selectedVenue.venueName || '—'}` : (selectedVenueName ? 'No exact match selected' : '—')}
            </div>
            <div className="muted" style={{ marginTop: 4, fontSize: 12 }}>
              Selected layout: {selectedLayout ? `${selectedLayout.layoutName || '—'}` : (selectedLayoutName ? 'No exact match selected' : '—')}
            </div>
          </div>
        </div>

        <div className="card" style={{ display: 'flex', flexDirection: 'column' }}>
          <h3 style={{ marginTop: 0 }}>Layout preview</h3>
          {!layoutView ? (
            <div className="muted">Load a layout to see its sections.</div>
          ) : (
            <>
              <div><b>Venue:</b> {layoutView.venueName}</div>
              <div><b>Layout:</b> {layoutView.layoutName}</div>
              <div className="muted" style={{ marginTop: 6 }}>
                Canvas: {layoutView.canvasWidth} × {layoutView.canvasHeight}
              </div>
              <div style={{ marginTop: 10, height: 'clamp(280px, 45vh, 460px)' }}>
                <div className="muted" style={{ fontSize: 12, marginBottom: 6 }}>
                  Preview (canvas + seats from section x/y). Drag to pan.
                </div>
                <LayoutSeatPreview layoutView={layoutView} draftSection={draftSectionForPreview} />
              </div>
              <div className="muted" style={{ marginTop: 6 }}>
                Sections: {Array.isArray(layoutView.sections) ? layoutView.sections.length : 0}
              </div>
            </>
          )}
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div className="card">
          <h3 style={{ marginTop: 0 }}>Add section</h3>
          <div className="muted" style={{ fontSize: 12, marginTop: -6, marginBottom: 8 }}>
            Step 4: add a section (x/y/width/height). The preview updates live.
          </div>
          <div className="form" style={{ margin: 0 }}>
            <label className="muted">Section name</label>
            <input value={sectionForm.sectionName} onChange={(e) => setSectionForm(s => ({ ...s, sectionName: e.target.value }))} placeholder="Section name" />
            <label className="muted">Section type</label>
            <select value={sectionForm.sectionType} onChange={(e) => setSectionForm(s => ({ ...s, sectionType: e.target.value }))}>
              {SECTION_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
            </select>

            <label className="muted">Position (x, y)</label>
            <div style={{ display: 'flex', gap: 8 }}>
              <input type="number" value={sectionForm.x} onChange={(e) => setSectionForm(s => ({ ...s, x: e.target.value }))} placeholder="x" />
              <input type="number" value={sectionForm.y} onChange={(e) => setSectionForm(s => ({ ...s, y: e.target.value }))} placeholder="y" />
            </div>

            <label className="muted">Size (width, height)</label>
            <div style={{ display: 'flex', gap: 8 }}>
              <input type="number" value={sectionForm.width} onChange={(e) => setSectionForm(s => ({ ...s, width: e.target.value }))} placeholder="width" />
              <input type="number" value={sectionForm.height} onChange={(e) => setSectionForm(s => ({ ...s, height: e.target.value }))} placeholder="height" />
            </div>

            <label className="muted">Rotation</label>
            <div style={{ display: 'flex', gap: 8 }}>
              <input type="number" value={sectionForm.rotation} onChange={(e) => setSectionForm(s => ({ ...s, rotation: e.target.value }))} placeholder="rotation" />
              <label style={{ display: 'flex', alignItems: 'center', gap: 8, paddingTop: 6 }}>
                <input type="checkbox" checked={sectionForm.curved} onChange={(e) => setSectionForm(s => ({ ...s, curved: e.target.checked }))} />
                Curved
              </label>
            </div>

            <label className="muted">Rows (comma-separated)</label>
            <input value={sectionForm.rowsCsv} onChange={(e) => setSectionForm(s => ({ ...s, rowsCsv: e.target.value }))} placeholder="e.g. A,B,C" />
            <label className="muted">Seats per row</label>
            <input type="number" value={sectionForm.seatsPerRow} onChange={(e) => setSectionForm(s => ({ ...s, seatsPerRow: e.target.value }))} placeholder="e.g. 10" />

            {!selectedLayoutId ? (
              <div className="muted" style={{ fontSize: 12, marginTop: 6 }}>
                Pick a venue + layout first.
              </div>
            ) : null}
            <button className="btn" onClick={addSection} disabled={!selectedLayoutId}>Add section</button>
          </div>
          </div>

          <div className="card">
          <h3 style={{ marginTop: 0 }}>Create event</h3>
          <div className="muted" style={{ fontSize: 12, marginTop: -6, marginBottom: 8 }}>
            Step 5: create an event on a layout (so seats can be generated).
          </div>
          <div className="form" style={{ margin: 0 }}>
            <input value={eventForm.title} onChange={(e) => setEventForm(s => ({ ...s, title: e.target.value }))} placeholder="Event title" />
            <label className="muted">Date/time</label>
            <input type="datetime-local" value={eventForm.dateTime} onChange={(e) => setEventForm(s => ({ ...s, dateTime: e.target.value }))} />
            {eventForm.dateTime ? (
              <div className="muted" style={{ fontSize: 12, marginTop: 2 }}>
                Will display as: {formatDateTime(normalizeLocalDateTime(eventForm.dateTime))}
              </div>
            ) : null}

            <div style={{ display: 'flex', gap: 8 }}>
              <input type="number" value={eventForm.minResale} onChange={(e) => setEventForm(s => ({ ...s, minResale: e.target.value }))} placeholder="Min resale" />
              <input type="number" value={eventForm.maxResale} onChange={(e) => setEventForm(s => ({ ...s, maxResale: e.target.value }))} placeholder="Max resale" />
            </div>

            <textarea value={eventForm.description} onChange={(e) => setEventForm(s => ({ ...s, description: e.target.value }))} placeholder="Description" rows={3} />
            <label className="muted">Event image (file)</label>
            <input type="file" accept="image/*" onChange={(e) => onPickEventImage(e.target.files?.[0])} />
            {eventForm.imageUrl ? (
              <img src={eventForm.imageUrl} alt="Event preview" style={{ maxWidth: '100%', height: 'auto', borderRadius: 8, marginTop: 6 }} />
            ) : null}
            <label className="muted">Event image (URL)</label>
            <input value={eventForm.imageUrl} onChange={(e) => setEventForm(s => ({ ...s, imageUrl: e.target.value }))} placeholder="https://… or a data URL" />

            {!selectedLayoutId ? (
              <div className="muted" style={{ fontSize: 12, marginTop: 6 }}>
                Pick a venue and a layout to attach seats to this event.
              </div>
            ) : null}
            <button className="btn" onClick={createEvent} disabled={creatingEvent}>
              {creatingEvent ? 'Creating…' : 'Create event'}
            </button>
          </div>
          </div>
        </div>
      </div>
    </div>
  )
}
