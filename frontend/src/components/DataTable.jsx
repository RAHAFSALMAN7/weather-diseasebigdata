import React from "react";

function num(v) {
    const n = Number(v);
    return Number.isFinite(n) ? n : 0;
}

function HumidityBadge({ level }) {
    const cls =
        level === "High" ? "badge badge-high" :
            level === "Medium" ? "badge badge-med" :
                level === "Low" ? "badge badge-low" :
                    "badge";
    return <span className={cls}>{level || "Unknown"}</span>;
}

export default function DataTable({ rows }) {
    const box = {
        border: "1px solid var(--card-border)",
        borderRadius: "var(--radius)",
        padding: 14,
        background: "var(--card-bg)",
        boxShadow: "var(--shadow)",
    };

    return (
        <div style={box}>
            <h3 style={{ margin: "0 0 8px 0" }}>Results Table</h3>

            <div style={{ overflowX: "auto" }}>
                <table className="wd-table">
                    <thead>
                    <tr>
                        <th>country</th>
                        <th>season</th>
                        <th>disease</th>
                        <th>avg_infection_rate</th>
                        <th>avg_temperature</th>
                        <th>avg_feels_like</th>
                        <th>avg_humidity</th>
                        <th>avg_wind_speed</th>
                        <th>avg_pressure</th>
                        <th>avg_cloudiness</th>
                        <th>humidity_level</th>
                        <th>weather_stress_index</th>
                        <th>ingested_at</th>
                    </tr>
                    </thead>

                    <tbody>
                    {rows.map((r) => (
                        <tr key={r._id || `${r.country}-${r.season}-${r.disease}-${r.ingested_at}`}>
                            <td>{r.country}</td>
                            <td>{r.season}</td>
                            <td>{r.disease}</td>
                            <td>{num(r.avg_infection_rate).toFixed(3)}</td>
                            <td>{num(r.avg_temperature).toFixed(2)}</td>
                            <td>{num(r.avg_feels_like).toFixed(2)}</td>
                            <td>{num(r.avg_humidity).toFixed(0)}</td>
                            <td>{num(r.avg_wind_speed).toFixed(2)}</td>
                            <td>{num(r.avg_pressure).toFixed(0)}</td>
                            <td>{num(r.avg_cloudiness).toFixed(0)}</td>
                            <td><HumidityBadge level={r.humidity_level} /></td>
                            <td>{num(r.weather_stress_index).toFixed(0)}</td>
                            <td>{r.ingested_at}</td>
                        </tr>
                    ))}

                    {rows.length === 0 && (
                        <tr>
                            <td colSpan={13} style={{ padding: 10, color: "var(--muted)" }}>
                                No data for selected filters.
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
