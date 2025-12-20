import React, { useMemo } from "react";

function num(v) {
    const n = Number(v);
    return Number.isFinite(n) ? n : 0;
}

function Card({ title, value, color }) {
    return (
        <div
            style={{
                border: "1px solid var(--card-border)",
                borderRadius: "var(--radius)",
                padding: 14,
                background: "var(--card-bg)",
                boxShadow: "var(--shadow)",
                borderLeft: `6px solid ${color}`,
            }}
        >
            <div style={{ fontSize: 12, color: "var(--muted)" }}>{title}</div>
            <div style={{ fontSize: 24, fontWeight: 800, marginTop: 6, color }}>{value}</div>
        </div>
    );
}

export default function KpiRow({ rows }) {
    const kpi = useMemo(() => {
        if (!rows.length) return { count: 0, inf: 0, temp: 0, hum: 0, stress: 0 };

        const s = rows.reduce(
            (acc, r) => {
                acc.inf += num(r.avg_infection_rate);
                acc.temp += num(r.avg_temperature);
                acc.hum += num(r.avg_humidity);
                acc.stress += num(r.weather_stress_index);
                return acc;
            },
            { inf: 0, temp: 0, hum: 0, stress: 0 }
        );

        return {
            count: rows.length,
            inf: s.inf / rows.length,
            temp: s.temp / rows.length,
            hum: s.hum / rows.length,
            stress: s.stress / rows.length,
        };
    }, [rows]);

    return (
        <div
            style={{
                display: "grid",
                gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
                gap: 12,
                marginTop: 12,
            }}
        >
            <Card title="Records" value={kpi.count} color="var(--primary)" />
            <Card title="Avg Infection Rate" value={kpi.inf.toFixed(2)} color="var(--danger)" />
            <Card title="Avg Temperature (°C)" value={kpi.temp.toFixed(2)} color="var(--warning)" />
            <Card title="Avg Humidity (%)" value={kpi.hum.toFixed(0)} color="var(--accent-1)" />
            <Card title="Avg Weather Stress" value={kpi.stress.toFixed(2)} color="var(--secondary)" />
        </div>
    );
}
