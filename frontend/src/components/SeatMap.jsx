export default function SeatMap({ seats, selectedSeatId, onSelectSeat }) {
  // group by row
  const byRow = seats.reduce((acc, s) => {
    const rowKey = s.row ?? "?";
    acc[rowKey] = acc[rowKey] || [];
    acc[rowKey].push(s);
    return acc;
  }, {});

  const rowKeys = Object.keys(byRow).sort((a, b) => Number(a) - Number(b));

  return (
    <div style={{ border: "1px solid #333", borderRadius: 10, padding: 12 }}>
      <h3 style={{ marginTop: 0 }}>Seat Map</h3>

      {rowKeys.length === 0 ? <p>No seats returned.</p> : null}

      <div style={{ display: "grid", gap: 10 }}>
        {rowKeys.map((row) => {
          const rowSeats = byRow[row].sort((a, b) => (a.number ?? 0) - (b.number ?? 0));
          return (
            <div key={row} style={{ display: "flex", gap: 6, alignItems: "center", flexWrap: "wrap" }}>
              <strong style={{ width: 60 }}>Row {row}</strong>

              {rowSeats.map((s) => {
                const isSelected = s.id === selectedSeatId;
                const disabled = s.available === false;

                return (
                  <button
                    key={s.id}
                    disabled={disabled}
                    onClick={() => onSelectSeat(s)}
                    title={s.label ?? `Row ${s.row} Seat ${s.number}`}
                    style={{
                      padding: "8px 10px",
                      borderRadius: 8,
                      border: "1px solid #444",
                      cursor: disabled ? "not-allowed" : "pointer",
                      opacity: disabled ? 0.35 : 1,
                      outline: isSelected ? "2px solid #7aa2ff" : "none",
                    }}
                  >
                    {s.number ?? "?"}
                  </button>
                );
              })}
            </div>
          );
        })}
      </div>

      <div style={{ marginTop: 10, opacity: 0.8 }}>
        <span>Selected: </span>
        <strong>{selectedSeatId ?? "none"}</strong>
      </div>
    </div>
  );
}
