import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const auth = useAuth();
  const user = auth?.user;
  const setUser = auth?.setUser;
  const navigate = useNavigate();

  function logout() {
    setUser(null);      // clear auth state
    navigate("/");      // redirect to home
  }

  return (
    <nav style={{ padding: 10, borderBottom: "1px solid #333" }}>
      <Link to="/">Home</Link>{" "}

      {user && <Link to="/search">Search</Link>}

      {user?.role === "ORGANIZER" && (
        <> | <Link to="/organizer">Organizer</Link></>
      )}

      {user?.role === "ADMIN" && (
        <> | <Link to="/admin">Admin</Link></>
      )}

      <span style={{ float: "right" }}>
        {user ? (
          <>
            {user.role} |{" "}
            <button onClick={logout}>Logout</button>
          </>
        ) : (
          <Link to="/login">Login</Link>
        )}
      </span>
    </nav>
  );
}
