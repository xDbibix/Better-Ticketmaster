export default function TicketFilters({ filters, setFilters }) {
  return (
    <div style={{ border: "1px solid #333", borderRadius: 10, padding: 12 }}>
      <h3 style={{ marginTop: 0 }}>Filters</h3>

      <div style={{ display: "grid", gap: 10, maxWidth: 520 }}>
        <label>
          Ticket type:
          <select
            value={filters.type}
            onChange={(e) => setFilters({ ...filters, type: e.target.value })}
          >
            <option value="">Any</option>
            <option value="STANDARD">Standard</option>
            <option value="VIP">VIP</option>
            <option value="RESALE">Resale</option>
          </select>
        </label>

        <label>
          Min price:
          <input
            type="number"
            value={filters.minPrice}
            onChange={(e) =>
              setFilters({ ...filters, minPrice: e.target.value })
            }
          />
        </label>

        <label>
          Max price:
          <input
            type="number"
            value={filters.maxPrice}
            onChange={(e) =>
              setFilters({ ...filters, maxPrice: e.target.value })
            }
          />
        </label>
      </div>
    </div>
  );
}
