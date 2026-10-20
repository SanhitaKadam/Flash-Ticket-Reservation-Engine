// Groups seats by row and draws one button per seat.
// States: AVAILABLE (green), selected (blue), HELD (amber, used from Phase 3), BOOKED (red).
export default function SeatMap({ seats, selected, onToggle }) {
  const rows = new Map()
  for (const s of seats) {
    if (!rows.has(s.row)) rows.set(s.row, [])
    rows.get(s.row).push(s)
  }

  return (
    <div className="seatmap">
      <div className="stage"><span>STAGE</span></div>
      <div className="rows">
        {[...rows.entries()].map(([row, list]) => (
          <div className="seat-row" key={row}>
            <span className="row-label">{row}</span>
            <div className="seat-list">
              {list.map((s) => {
                const isSelected = selected.has(s.id)
                const state = isSelected ? 'selected' : s.status.toLowerCase()
                const disabled = s.status !== 'AVAILABLE'
                return (
                  <button
                    key={s.id}
                    type="button"
                    className={`seat seat-${state}`}
                    disabled={disabled}
                    onClick={() => onToggle(s)}
                    aria-pressed={isSelected}
                    aria-label={`Seat ${s.label}, ${s.status.toLowerCase()}`}
                    title={`${s.label} · ₹${s.price}`}
                  >
                    {s.number}
                  </button>
                )
              })}
            </div>
            <span className="row-label">{row}</span>
          </div>
        ))}
      </div>
      <ul className="legend">
        <li><i className="seat-dot seat-available" />Available</li>
        <li><i className="seat-dot seat-selected" />Selected</li>
        <li><i className="seat-dot seat-held" />Held</li>
        <li><i className="seat-dot seat-booked" />Booked</li>
      </ul>
    </div>
  )
}
