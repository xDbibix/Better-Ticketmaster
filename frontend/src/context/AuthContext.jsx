import React, { createContext, useContext, useEffect, useState } from 'react'
import api from '../api/axiosConfig'

export const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)

  useEffect(() => {
    api.get('/auth/me').then(r => setUser(r.data)).catch(() => setUser(null))
  }, [])

  const login = (email, password) =>
    api.post('/auth/login', { email, password }).then(r => {
      setUser(r.data)
      return r.data
    })

  const register = (email, password, role) =>
    api.post('/auth/register', { email, password, role }).then(r => {
      setUser(r.data)
      return r.data
    })

  const logout = () => api.post('/auth/logout').then(() => setUser(null))

  return <AuthContext.Provider value={{ user, login, register, logout }}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return ctx
}
