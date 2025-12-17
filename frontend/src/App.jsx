import { Routes, Route } from "react-router-dom";
import Navbar from "./components/NavBar";
import RoleGuard from "./components/RoleGuard";

import Login from "./pages/Login";
import Search from "./pages/Search";
import EventPage from "./pages/Event";
import Checkout from "./pages/Checkout";
import OrganizerDashboard from "./pages/OrganizerDashboard";
import AdminDashboard from "./pages/AdminDashboard";

function App() {
  return (
    <>
      <Navbar />

      <Routes>
        <Route path="/" element={<h1>Welcome</h1>} />
        <Route path="/login" element={<Login />} />

        <Route
          path="/search"
          element={
            <RoleGuard allow={["CONSUMER", "ORGANIZER", "ADMIN"]}>
              <Search />
            </RoleGuard>
          }
        />

        <Route
          path="/event/:id"
          element={
            <RoleGuard allow={["CONSUMER"]}>
              <EventPage />
            </RoleGuard>
          }
        />

        <Route
          path="/checkout"
          element={
            <RoleGuard allow={["CONSUMER"]}>
              <Checkout />
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
          path="/admin"
          element={
            <RoleGuard allow={["ADMIN"]}>
              <AdminDashboard />
            </RoleGuard>
          }
        />
      </Routes>
    </>
  );
}

export default App;
