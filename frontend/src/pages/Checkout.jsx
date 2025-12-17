import { useState } from "react";
import { bookTicket } from "../api/bookingApi";

export default function Checkout() {
  const [status, setStatus] = useState("idle");
  const [msg, setMsg] = useState("");

  const eventId = "demo-event";
  const seat = { id: "demo-seat" };
  const ticket = null;

  async function confirmPurchase() {
    setStatus("loading");
    setMsg("");

    try {
      const result = await bookTicket({
        eventId,
        seatId: seat.id,
        ticketId: ticket?.id ?? null,
      });

      setStatus("success");
      setMsg(`Purchase complete! Booking ID: ${result.bookingId}`);
    } catch (e) {
      setStatus("error");
      setMsg("Booking failed. Please try again.");
    }
  }

  return (
    <div style={{ padding: 20 }}>
      <h1>Checkout</h1>

      <button onClick={confirmPurchase} disabled={status === "loading"}>
        {status === "loading" ? "Processing..." : "Confirm Purchase"}
      </button>

      {msg && <p>{msg}</p>}
    </div>
  );
}
