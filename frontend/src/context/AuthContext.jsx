import { createContext, useContext, useState } from "react";

const AuthContext = createContext({
  user: null,
  setUser: () => {},
});


export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  // user = { role: "CONSUMER" | "ORGANIZER" | "ADMIN", email }

  return (
    <AuthContext.Provider value={{ user, setUser }}>
      {children}
    </AuthContext.Provider>
  );
}

// 👇 THIS is what Navbar is trying to import
export function useAuth() {
  return useContext(AuthContext);
}
