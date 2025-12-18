import React, { useEffect, useMemo, useRef, useState, useImperativeHandle, forwardRef } from 'react';
import api from '../api/axiosConfig';

const SeatMap = forwardRef(function SeatMap({ eventId, selectedSeats, setSelectedSeats }, ref) {
  const [seats, setSeats] = useState([]);
  const [loading, setLoading] = useState(true);
  const containerRef = useRef(null);
  const [containerWidth, setContainerWidth] = useState(0);

  const loadSeats = () => {
    if (!eventId) return;
    setLoading(true);
    api.get(`/seats?eventId=${eventId}`)
      .then(r => setSeats(r.data))
      .catch(() => setSeats([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadSeats();
    // eslint-disable-next-line
  }, [eventId]);

  useEffect(() => {
    // Ensure the element exists before observing.
    // This effect may run while we are still rendering the loading/empty states.
    const el = containerRef.current;
    if (!el) return;

    const update = () => setContainerWidth(el.getBoundingClientRect().width || 0);
    update();

    // Prefer ResizeObserver when available.
    let ro;
    if (typeof ResizeObserver !== 'undefined') {
      ro = new ResizeObserver(() => update());
      ro.observe(el);
    } else {
      window.addEventListener('resize', update);
    }
    return () => {
      if (ro) ro.disconnect();
      else window.removeEventListener('resize', update);
    };
  }, [eventId, loading, seats.length]);

  useImperativeHandle(ref, () => ({ reload: loadSeats }), [eventId]);

  // Group seats by row for display
  const rows = useMemo(() => {
    const out = {};
    seats.forEach(seat => {
      const key = String(seat.row ?? '');
      if (!out[key]) out[key] = [];
      out[key].push(seat);
    });
    return out;
  }, [seats]);

  const rowKeys = useMemo(() => Object.keys(rows).sort(), [rows]);
  const maxSeatsInRow = useMemo(() => {
    let max = 0;
    for (const k of rowKeys) max = Math.max(max, rows[k]?.length || 0);
    return max;
  }, [rowKeys, rows]);

  // Color codes
  const COLOR_AVAILABLE = '#0b57d0'; // blue
  const COLOR_UNAVAILABLE = '#aaa'; // gray
  const COLOR_SELECTED = '#ff9800'; // orange for selected

  const seatSize = useMemo(() => {
    // Compute a seat size that fits the longest row.
    // Keep a reasonable clamp so seats remain clickable.
    const LABEL_W = 28;
    const GAP = 2;
    const H_PADDING = 8;
    const available = Math.max(0, (containerWidth || 0) - LABEL_W - H_PADDING);
    if (!maxSeatsInRow) return 28;
    const raw = Math.floor((available - (maxSeatsInRow - 1) * GAP) / maxSeatsInRow);
    return Math.max(18, Math.min(34, raw || 28));
  }, [containerWidth, maxSeatsInRow]);

  const seatFontSize = Math.max(10, Math.min(14, Math.floor(seatSize * 0.45)));

  if (loading) return <div>Loading seats...</div>;
  if (!seats.length) return <div>No seats found for this event.</div>;

  return (
    <div>
      <h4>Seat Map</h4>
      <div
        ref={containerRef}
        style={{
          display: 'flex',
          flexDirection: 'column',
          gap: 6,
          overflowX: 'auto',
          paddingBottom: 4,
        }}
      >
        {rowKeys.map(row => (
          <div key={row} style={{ display: 'flex', alignItems: 'center', gap: 2, minWidth: 'max-content' }}>
            <span style={{ width: 24 }}>{row}</span>
            {rows[row].sort((a, b) => a.seatNum - b.seatNum).map(seat => {
              const isSelected = selectedSeats.includes(seat.id);
              const isUnavailable = seat.status === 'SOLD' || seat.status === 'HELD';
              let bg = isUnavailable ? COLOR_UNAVAILABLE : (isSelected ? COLOR_SELECTED : COLOR_AVAILABLE);
              let color = isUnavailable ? '#fff' : (isSelected ? '#fff' : '#fff');
              return (
                <button
                  key={seat.id}
                  disabled={isUnavailable}
                  style={{
                    width: seatSize,
                    height: seatSize,
                    margin: 1,
                    background: bg,
                    color: color,
                    border: isSelected ? '2px solid #ff9800' : '1px solid #ccc',
                    cursor: isUnavailable ? 'not-allowed' : 'pointer',
                    fontWeight: isSelected ? 700 : 400,
                    fontSize: seatFontSize,
                    lineHeight: 1,
                    padding: 0,
                  }}
                  onClick={() => {
                    setSelectedSeats((prev) => {
                      const current = Array.isArray(prev) ? prev : [];
                      if (current.includes(seat.id)) return current.filter(id => id !== seat.id);
                      return [...current, seat.id];
                    });
                  }}
                  title={`Row ${seat.row} Seat ${seat.seatNum} - $${seat.price}`}
                >
                  {seat.seatNum}
                </button>
              );
            })}
          </div>
        ))}
      </div>
      <div style={{ marginTop: 8 }}>
        <span style={{ background: COLOR_AVAILABLE, color: '#fff', padding: '2px 6px', borderRadius: 4 }}>Available</span>
        <span style={{ background: COLOR_UNAVAILABLE, color: '#fff', padding: '2px 6px', borderRadius: 4, marginLeft: 8 }}>Unavailable</span>
        <span style={{ background: COLOR_SELECTED, color: '#fff', padding: '2px 6px', borderRadius: 4, marginLeft: 8 }}>Selected</span>
      </div>
    </div>
  );
});

export default SeatMap;
