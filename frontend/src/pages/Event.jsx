import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../api/api";
import SeatMap from "../components/SeatMap";
import TicketFilters from "../components/TicketFilters";

export default function EventPage() {
  const { id } = useParams();
  const nav = useNavigate();

  const [event, setEvent] = useState(null);
  const [seats, setSeats] = useState([]);
  const [tickets, setTickets] = useState([]);

  const [selectedSeat, setSelectedSeat] = useState(null);
  const [filters, setFilters] = useState({ type: "", minPrice: "", maxPrice: "", sort: "" });
  const [loading, setLoading] = useState(true);

  // load event + seats
  useEffect(() => {
    setLoading(true);
    Promise.allSettled([
      api.get(`/api/events/${id}`),          // ok if you don't have it; we ignore failure
      api.get(`/api/events/${id}/seats`),
    ]).then((results) => {
      const [eventRes, seatsRes] = results;

      if (eventRes.status === "fulfilled") setEvent(eventRes.value.data);
      if (seatsRes.status === "fulfilled") setSeats(seatsRes.value.data ?? []);
    }).finally(() => setLoading(false));
  }, [id]);

  // load tickets when filters change
  useEffect(() => {
    const params = {};
    if (filters.type) params.type = filters.type;
    if (filters.minPrice !== "") params.minPrice = filters.minPrice;
    if (filters.maxPrice !== "") params.maxPrice = filters.maxPrice;
    if (filters.sort) params.sort = filters.sort;

    api.get(`/api/events/${id}/tickets`, { params })
      .then(res => setTickets(res.data ?? []))
      .catch(() => setTickets([]));
  }, [id, filters]);

  // If backend doesn't provide tickets, we can derive a “ticket preview” from seat selection later.
  const selectedTicket = useMemo(() => {
    if (!selectedSeat) return null;
    // If your backend returns a ticket for each seat, match it:
    const t = tickets.find(t => t.seatId === selectedSeat.id) || null;
    return t;
  }, [selectedSeat, tickets]);

  function goCheckout() {
    if (!selectedSeat) return;

    nav("/checkout", {
      state: {
        eventId: id,
        eventName: event?.name ?? "Event",
        seat: selectedSeat,
        ticket: selectedTicket, // may be null, that's OK (we’ll handle)
      },
    });
  }

  return (
    <div style={{ padding: 20 }}>
      <h1>{event?.name ?? `Event ${id}`}</h1>

      {loading ? <p>Loading event…</p> : null}

      <div style={{ display: "grid", gap: 12, gridTemplateColumns: "1fr", maxWidth: 1000 }}>
        <TicketFilters filters={filters} setFilters={setFilters} />

        <SeatMap
          seats={seats}
          selectedSeatId={selectedSeat?.id ?? null}
          onSelectSeat={(s) => setSelectedSeat(s)}
        />

        <button
          onClick={goCheckout}
          disabled={!selectedSeat}
          style={{ padding: 12, borderRadius: 10, border: "1px solid #444" }}
        >
          Continue to Checkout →
        </button>

        <div style={{ opacity: 0.85 }}>
          <strong>Tip:</strong> if tickets list is empty but seats show, you can still proceed —
          checkout will call the backend booking endpoint using the seat/ticket id.
        </div>
      </div>
    </div>
  );
}
