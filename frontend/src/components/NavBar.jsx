import React, {useContext} from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { AuthContext } from '../context/AuthContext'

export default function Navbar(){
  const {user,logout} = useContext(AuthContext)
  const nav = useNavigate()
  return (
    <header className="nav">
      <div className="nav-left">
        <Link to="/" style={{fontWeight:700,fontSize:18}}>BetterTicket</Link>
        <span className="muted">Buy tickets & transfer</span>
      </div>
      <div className="nav-right">
        {user ? (
          <>
            <nav className="nav-links" aria-label="Primary">
              <Link to="/search">Search</Link>
              <Link to="/tickets">My Tickets</Link>
              {user.role === 'ORGANIZER' ? <Link to="/organizer/events">My Events</Link> : null}
              {user.role === 'ORGANIZER' ? <Link to="/organizer">Create Event</Link> : null}
              {user.role === 'ADMIN' ? <Link to="/admin">Admin</Link> : null}
            </nav>
            <span className="nav-email" title={user.email}>{user.email}</span>
            <button className="nav-linklike" onClick={()=>{logout();nav('/')}} type="button">Logout</button>
          </>
        ) : (
          <>
            <nav className="nav-links" aria-label="Auth">
              <Link to="/login">Login</Link>
              <Link to="/register">Register</Link>
            </nav>
          </>
        )}
      </div>
    </header>
  )
}
