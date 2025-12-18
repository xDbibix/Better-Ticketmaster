import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/axiosConfig";
import TicketFilters from "../components/TicketFilters";

function toNumberOrNull(v) {
  const n = Number.parseFloat(String(v ?? "").trim());
  return Number.isFinite(n) ? n : null;
}

export default function Search() {
  const [events, setEvents] = useState([]);
  const [q, setQ] = useState("");
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({ type: "", minPrice: "", maxPrice: "" });
  const [resaleByEventId, setResaleByEventId] = useState({});
  const [resaleLoading, setResaleLoading] = useState(false);

  useEffect(() => {
    api.get("/events")
      .then(res => {
        const list = Array.isArray(res.data) ? res.data : [];
        const filtered = list.filter(e => {
          const status = String(e?.status || "").trim().toUpperCase();
          if (!status) return true;
          if (status === "CLOSED") return false;
          if (status === "REJECTED") return false;
          // For demo usability, show pending events too.
          return status === "APPROVED" || status === "PENDING";
        });
        setEvents(filtered);
      })
      .finally(() => setLoading(false));
  }, []);

  const textFiltered = useMemo(() => {
    const query = q.trim().toLowerCase();
    if (!query) return events;
    return events.filter(e =>
      (e.title ?? e.name ?? "").toLowerCase().includes(query) ||
      (e.venueName ?? "").toLowerCase().includes(query)
    );
  }, [events, q]);

  const needsResale = useMemo(() => {
    const min = toNumberOrNull(filters.minPrice);
    const max = toNumberOrNull(filters.maxPrice);
    return filters.type === "RESALE" || min != null || max != null;
  }, [filters.maxPrice, filters.minPrice, filters.type]);

  useEffect(() => {
    if (!needsResale) return;
    if (!textFiltered.length) return;

    const missing = textFiltered
      .map(e => e.id)
      .filter(id => id && resaleByEventId[id] === undefined);

    if (!missing.length) return;

    let cancelled = false;
    setResaleLoading(true);

    Promise.all(
      missing.map(async (eventId) => {
        try {
          const r = await api.get(`/tickets/resale?eventId=${eventId}`);
          const list = Array.isArray(r.data) ? r.data : [];
          const prices = list.map(t => Number(t.resalePrice)).filter(n => Number.isFinite(n));
          const count = list.length;
          const minPrice = prices.length ? Math.min(...prices) : null;
          const maxPrice = prices.length ? Math.max(...prices) : null;
          return [eventId, { count, minPrice, maxPrice }];
        } catch {
          return [eventId, { count: 0, minPrice: null, maxPrice: null }];
        }
      })
    )
      .then(entries => {
        if (cancelled) return;
        setResaleByEventId(prev => {
          const next = { ...prev };
          for (const [eventId, data] of entries) next[eventId] = data;
          return next;
        });
      })
      .finally(() => {
        if (!cancelled) setResaleLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [needsResale, resaleByEventId, textFiltered]);

  const filtered = useMemo(() => {
    const min = toNumberOrNull(filters.minPrice);
    const max = toNumberOrNull(filters.maxPrice);

    // This project doesn't implement STANDARD/VIP listings explicitly; treat them like "Any".
    const requiresResale = filters.type === "RESALE" || min != null || max != null;

    if (!requiresResale) return textFiltered;

    return textFiltered.filter(e => {
      const stats = resaleByEventId[e.id];
      if (!stats) return false; // stats not loaded yet
      if (!stats.count) return false;

      // Price range overlap check using event resale min/max.
      // If we know min/max of listed tickets, use them to see if any listing could match.
      const listedMin = stats.minPrice;
      const listedMax = stats.maxPrice;
      if (min != null && listedMax != null && listedMax < min) return false;
      if (max != null && listedMin != null && listedMin > max) return false;
      return true;
    });
  }, [filters.maxPrice, filters.minPrice, filters.type, resaleByEventId, textFiltered]);

  return (
    <div>
      <div className="card">
        <h1 style={{ marginTop: 0 }}>Search Events</h1>
        <input
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="Search by event or venue..."
        />
      </div>

      <div style={{ marginTop: 16 }}>
        <TicketFilters filters={filters} setFilters={setFilters} />
        {needsResale && resaleLoading ? <p className="muted">Loading resale data…</p> : null}
      </div>

      {loading ? <p>Loading…</p> : null}

      {!loading && filtered.length === 0 ? <p>No events found.</p> : null}

      <div className="grid" style={{ marginTop: 16 }}>
        {filtered.map((e) => {
          const stats = resaleByEventId[e.id];
          return (
            <div key={e.id} className="card">
              <h3 style={{ margin: 0 }}>{e.title ?? e.name ?? "Untitled Event"}</h3>
              <p className="muted" style={{ margin: "6px 0" }}>
                {e.venueName ? `Venue: ${e.venueName}` : ""}
              </p>
              {needsResale ? (
                <p className="muted" style={{ margin: "6px 0" }}>
                  {stats
                    ? (stats.count
                        ? `Resale listings: ${stats.count}${stats.minPrice != null ? ` (from $${stats.minPrice})` : ""}`
                        : "Resale listings: none")
                    : "Resale listings: loading…"}
                </p>
              ) : null}
              <Link to={`/event/${e.id}`}>View tickets & seats →</Link>
            </div>
          );
        })}
      </div>
    </div>
  );
}
