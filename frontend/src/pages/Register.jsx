import React, { useState, useContext } from 'react'
import { useNavigate } from 'react-router-dom'
import { AuthContext } from '../context/AuthContext'

export default function Register(){
  const [email,setEmail]=useState('')
  const [password,setPassword]=useState('')
  const [role,setRole]=useState('CONSUMER')
  const {register} = useContext(AuthContext)
  const nav = useNavigate()
  const submit = async e=>{
    e.preventDefault();
    try{ await register(email,password,role); nav('/')}catch(err){alert('Register failed')}
  }
  return (
    <div className="form card">
      <h3>Register</h3>
      <form onSubmit={submit}>
        <input value={email} onChange={e=>setEmail(e.target.value)} placeholder="Email" />
        <input value={password} onChange={e=>setPassword(e.target.value)} placeholder="Password" type="password" />
        <select value={role} onChange={e=>setRole(e.target.value)}>
          <option value="CONSUMER">Consumer</option>
          <option value="ORGANIZER">Organizer</option>
        </select>
        <button className="btn" type="submit">Register</button>
      </form>
    </div>
  )
}
