import React, { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import api from '../api/axiosConfig'
import { useAuth } from '../context/AuthContext'

export default function Checkout(){
  const {id} = useParams()
  const nav = useNavigate()
  const [event,setEvent]=useState(null)
  const { user } = useAuth()
  useEffect(()=>{api.get(`/events/${id}`).then(r=>setEvent(r.data)).catch(()=>setEvent(null))},[id])

  const buy = async ()=>{
    if (!user) {
      nav('/login')
      return
    }
    try{
      const booking = { eventId: id, seatIds: [], totalPrice: 10.0 }
      let r = await api.post('/bookings', booking)
      await api.post(`/bookings/${r.data.id}/complete`)
      alert('Purchase complete — confirmation sent (or logged)')
      nav('/')
    }catch(err){alert('Purchase failed')}
  }

  if(!event) return <div>Loading...</div>
  return (
    <div className="card">
      <h3>Checkout — {event.title || event.name}</h3>
      <p className="muted">Total: $10.00</p>
      {!user ? (
        <button className="btn" onClick={() => nav('/login')}>Sign in to complete purchase</button>
      ) : (
        <button className="btn" onClick={buy}>Complete Purchase</button>
      )}
    </div>
  )
}
