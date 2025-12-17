/**
 * Simulated booking API
 * Replace implementation when backend booking endpoint is ready
 */
export async function bookTicket({ eventId, seatId, ticketId }) {
  await new Promise((resolve) => setTimeout(resolve, 800));

  return {
    bookingId: crypto.randomUUID(),
    eventId,
    seatId,
    ticketId,
  };
}
