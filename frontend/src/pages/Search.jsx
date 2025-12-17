import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/api";

export default function Search() {
  const [events, setEvents] = useState([]);
  const [q, setQ] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get("/api/events")
      .then(res => setEvents(res.data))
      .finally(() => setLoading(false));
  }, []);

  const filtered = useMemo(() => {
    const query = q.trim().toLowerCase();
    if (!query) return events;
    return events.filter(e =>
      (e.name ?? "").toLowerCase().includes(query) ||
      (e.venueName ?? "").toLowerCase().includes(query)
    );
  }, [events, q]);

  return (
    <div style={{ padding: 20 }}>
      <h1>Search Events</h1>

      <input
        value={q}
        onChange={(e) => setQ(e.target.value)}
        placeholder="Search by event or venue..."
        style={{ padding: 10, width: "100%", maxWidth: 480 }}
      />

      {loading ? <p>Loading…</p> : null}

      {!loading && filtered.length === 0 ? <p>No events found.</p> : null}

      <div style={{ marginTop: 16, display: "grid", gap: 12, maxWidth: 800 }}>
        {filtered.map((e) => (
          <div key={e.id} style={{ border: "1px solid #333", borderRadius: 10, padding: 12 }}>
            <h3 style={{ margin: 0 }}>{e.name ?? "Untitled Event"}</h3>
            <p style={{ margin: "6px 0", opacity: 0.8 }}>
              {e.venueName ? `Venue: ${e.venueName}` : ""}
            </p>
            <Link to={`/event/${e.id}`}>View tickets & seats →</Link>
          </div>
        ))}
      </div>
    </div>
  );
}
