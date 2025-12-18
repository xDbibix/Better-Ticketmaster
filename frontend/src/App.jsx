import React from 'react'
import { Routes, Route } from 'react-router-dom'
import Home from './pages/Home'
import Event from './pages/Event'
import Login from './pages/Login'
import Register from './pages/Register'
import Checkout from './pages/Checkout'
import Search from './pages/Search'
import MyTickets from './pages/MyTickets'
import OrganizerDashboard from './pages/OrganizerDashboard'
import OrganizerMyEvents from './pages/OrganizerMyEvents'
import AdminDashboard from './pages/AdminDashboard'
import RoleGuard from './components/RoleGuard'
import { AuthProvider } from './context/AuthContext'
import Navbar from './components/NavBar'

export default function App() {
  return (
    <AuthProvider>
      <div className="app">
        <Navbar />
        <main>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/event/:id" element={<Event />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/checkout/:id" element={<Checkout />} />

            <Route
              path="/search"
              element={
                <RoleGuard allow={["CONSUMER", "ORGANIZER", "ADMIN"]}>
                  <Search />
                </RoleGuard>
              }
            />

            <Route
              path="/tickets"
              element={
                <RoleGuard allow={["CONSUMER", "ORGANIZER", "ADMIN"]}>
                  <MyTickets />
                </RoleGuard>
              }
            />

            <Route
              path="/organizer"
              element={
                <RoleGuard allow={["ORGANIZER"]}>
                  <OrganizerDashboard />
                </RoleGuard>
              }
            />

            <Route
              path="/organizer/events"
              element={
                <RoleGuard allow={["ORGANIZER"]}>
                  <OrganizerMyEvents />
                </RoleGuard>
              }
            />

            <Route
              path="/admin"
              element={
                <RoleGuard allow={["ADMIN"]}>
                  <AdminDashboard />
                </RoleGuard>
              }
            />
          </Routes>
        </main>
      </div>
    </AuthProvider>
  )
}
