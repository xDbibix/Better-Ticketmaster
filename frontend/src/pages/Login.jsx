import React, { useState, useContext } from 'react'
import { useNavigate } from 'react-router-dom'
import { AuthContext } from '../context/AuthContext'

export default function Login(){
  const [email,setEmail]=useState('')
  const [password,setPassword]=useState('')
  const {login} = useContext(AuthContext)
  const nav = useNavigate()
  const submit = async e=>{
    e.preventDefault();
    try{ await login(email,password); nav('/')}catch(err){alert('Login failed')}
  }
  return (
    <div className="form card">
      <h3>Login</h3>
      <p className="muted">Demo login: consumer@btm.test / password</p>
      <form onSubmit={submit}>
        <input value={email} onChange={e=>setEmail(e.target.value)} placeholder="Email" />
        <input value={password} onChange={e=>setPassword(e.target.value)} placeholder="Password" type="password" />
        <button className="btn" type="submit">Login</button>
      </form>
    </div>
  )
}
