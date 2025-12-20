import React, { useMemo } from "react";
import {
    ResponsiveContainer,
    BarChart,
    Bar,
    XAxis,
    YAxis,
    Tooltip,
    CartesianGrid,
    ScatterChart,
    Scatter,
    ZAxis,
} from "recharts";

function num(v) {
    const n = Number(v);
    return Number.isFinite(n) ? n : 0;
}

export default function Charts({ rows, selectedCountry }) {
    const barData = useMemo(() => {
        const groupKey = selectedCountry && selectedCountry !== "all" ? "disease" : "country";

        const m = new Map();
        for (const r of rows) {
            const key = r[groupKey] || "unknown";
            const cur = m.get(key) || { name: key, sum: 0, n: 0 };
            cur.sum += num(r.avg_infection_rate);
            cur.n += 1;
            m.set(key, cur);
        }

        return Array.from(m.values())
            .map((x) => ({ name: x.name, avg_infection_rate: x.n ? x.sum / x.n : 0 }))
            .sort((a, b) => b.avg_infection_rate - a.avg_infection_rate);
    }, [rows, selectedCountry]);

    const scatterData = useMemo(() => {
        return rows.map((r) => ({
            label: `${r.country}-${r.season}-${r.disease}`,
            temp: num(r.avg_temperature),
            infection: num(r.avg_infection_rate),
            stress: num(r.weather_stress_index),
        }));
    }, [rows]);

    const panel = {
        border: "1px solid var(--card-border)",
        borderRadius: "var(--radius)",
        padding: 14,
        background: "var(--card-bg)",
        boxShadow: "var(--shadow)",
    };

    return (
        <div style={{ display: "grid", gridTemplateColumns: "1fr", gap: 12 }}>
            <div style={panel}>
                <h3 style={{ margin: "0 0 8px 0" }}>
                    Avg Infection Rate by {selectedCountry !== "all" ? "Disease" : "Country"}
                </h3>

                <div style={{ width: "100%", height: 320 }}>
                    <ResponsiveContainer>
                        <BarChart data={barData}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="name" interval={0} angle={-20} textAnchor="end" height={60} />
                            <YAxis />
                            <Tooltip />
                            <Bar dataKey="avg_infection_rate" fill="var(--primary)" />
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            </div>

            <div style={panel}>
                <h3 style={{ margin: "0 0 8px 0" }}>Temperature vs Infection (bubble size = stress)</h3>

                <div style={{ width: "100%", height: 340 }}>
                    <ResponsiveContainer>
                        <ScatterChart>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis type="number" dataKey="temp" name="Temp" unit="°C" />
                            <YAxis type="number" dataKey="infection" name="Infection" />
                            <ZAxis type="number" dataKey="stress" range={[60, 300]} name="Stress" />
                            <Tooltip />
                            <Scatter data={scatterData} fill="var(--accent-2)" />
                        </ScatterChart>
                    </ResponsiveContainer>
                </div>
            </div>
        </div>
    );
}
