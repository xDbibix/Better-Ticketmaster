import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

export default function Login() {
  const { setUser } = useAuth();
  const navigate = useNavigate();

  function loginAs(role) {
    setUser({ role });
    navigate("/search");
  }

  return (
    <div style={{ padding: 20 }}>
      <h1>Login (Demo)</h1>

      <button onClick={() => loginAs("CONSUMER")}>
        Login as Consumer
      </button>
      <br /><br />

      <button onClick={() => loginAs("ORGANIZER")}>
        Login as Organizer
      </button>
      <br /><br />

      <button onClick={() => loginAs("ADMIN")}>
        Login as Admin
      </button>
    </div>
  );
}
