export function Loading({ label = 'Loading…' }) {
  return <div className="state"><span className="spinner" />{label}</div>
}

export function ErrorBox({ error, onRetry }) {
  return (
    <div className="state state-error">
      <strong>Something went wrong.</strong>
      <p>{error?.message}</p>
      <p className="muted">Is the Spring Boot service running on port 8080?</p>
      {onRetry && <button className="btn btn-ghost" onClick={onRetry}>Try again</button>}
    </div>
  )
}
