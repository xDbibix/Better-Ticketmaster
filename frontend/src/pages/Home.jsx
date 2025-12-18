import React, { useEffect, useState } from 'react'
import api from '../api/axiosConfig'
import { Link } from 'react-router-dom'

function isPublicEvent(e) {
  const status = String(e?.status || '').trim().toUpperCase()
  if (!status) return true
  if (status === 'CLOSED') return false
  if (status === 'REJECTED') return false
  // For demo usability, show pending events too.
  return status === 'APPROVED' || status === 'PENDING'
}

function formatDateTime(value) {
  if (!value) return ''
  try {
    // Spring LocalDateTime usually comes as "YYYY-MM-DDTHH:mm:ss".
    const s = String(value).trim()
    const m = s.match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?$/)
    const d = m
      ? new Date(Number(m[1]), Number(m[2]) - 1, Number(m[3]), Number(m[4]), Number(m[5]), m[6] ? Number(m[6]) : 0)
      : new Date(s)
    if (Number.isNaN(d.getTime())) return s
    return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(d)
  } catch {
    return String(value)
  }
}

export default function Home(){
  const [events,setEvents] = useState([])
  useEffect(()=>{
    api.get('/events')
      .then(r => {
        const list = Array.isArray(r.data) ? r.data : []
        setEvents(list.filter(isPublicEvent))
      })
      .catch(()=>setEvents([]))
  },[])
  return (
    <div>
      <div className="hero">
        <h1>Find tickets to your favourite events</h1>
        <p style={{color: 'gray'}}>Sign in to buy, transfer, or resell tickets.</p>
      </div>

      <h3>Featured</h3>
      <div className="grid grid-max-3">
        {events.map(e=> (
          <div className="card" key={e.id}>
            <h4>{e.title || e.name}</h4>
            <p className="muted">{e.location || e.venueName}</p>
            <div style={{display:'flex',justifyContent:'space-between',marginTop:8}}>
              <Link to={`/event/${e.id}`} className="btn">View</Link>
              <div className="muted">{formatDateTime(e.dateTime || e.date)}</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
