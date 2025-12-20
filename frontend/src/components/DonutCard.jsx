import React, { useMemo } from "react";
import { PieChart, Pie, Tooltip, ResponsiveContainer } from "recharts";

export default function DonutCard({ rows }) {
    const data = useMemo(() => {
        const m = new Map();
        for (const r of rows) {
            const key = r.humidity_level || "Unknown";
            m.set(key, (m.get(key) || 0) + 1);
        }
        return Array.from(m.entries()).map(([name, value]) => ({ name, value }));
    }, [rows]);

    const box = {
        border: "1px solid var(--card-border)",
        borderRadius: "var(--radius)",
        padding: 14,
        background: "var(--card-bg)",
        boxShadow: "var(--shadow)",
    };

    return (
        <div style={box}>
            <h3 style={{ margin: "0 0 8px 0" }}>Humidity Level Distribution</h3>

            <div style={{ width: "100%", height: 220 }}>
                <ResponsiveContainer>
                    <PieChart>
                        <Tooltip />
                        <Pie data={data} dataKey="value" nameKey="name" innerRadius={55} outerRadius={85} fill="var(--accent-1)" />
                    </PieChart>
                </ResponsiveContainer>
            </div>

            <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                {data.map((x) => {
                    const cls =
                        x.name === "High" ? "badge badge-high" :
                            x.name === "Medium" ? "badge badge-med" :
                                x.name === "Low" ? "badge badge-low" :
                                    "badge";
                    return (
                        <span key={x.name} className={cls}>
              {x.name}: {x.value}
            </span>
                    );
                })}
            </div>
        </div>
    );
}
