import React, { useMemo } from "react";
import { PieChart, Pie, Tooltip, ResponsiveContainer } from "recharts";

export default function PieCard({ rows }) {
    const { data, total } = useMemo(() => {
        const m = new Map();
        for (const r of rows) {
            const key = r.season || "Unknown";
            m.set(key, (m.get(key) || 0) + 1);
        }
        const arr = Array.from(m.entries()).map(([name, value]) => ({ name, value }));
        const sum = arr.reduce((s, x) => s + x.value, 0);
        return { data: arr, total: sum };
    }, [rows]);

    return (
        <div style={{ border: "1px solid #ddd", borderRadius: 12, padding: 12, background: "#fff" }}>
            <h3 style={{ margin: "0 0 8px 0" }}>Seasons Distribution</h3>

            <div style={{ width: "100%", height: 220 }}>
                <ResponsiveContainer>
                    <PieChart>
                        <Tooltip />
                        <Pie data={data} dataKey="value" nameKey="name" outerRadius={85} />
                    </PieChart>
                </ResponsiveContainer>
            </div>

            <div style={{ marginTop: 8, fontSize: 12 }}>
                {data.length === 0 ? (
                    <div style={{ opacity: 0.7 }}>No data</div>
                ) : (
                    data
                        .slice()
                        .sort((a, b) => b.value - a.value)
                        .map((x) => {
                            const pct = total ? ((x.value / total) * 100).toFixed(1) : "0.0";
                            return (
                                <div key={x.name} style={{ display: "flex", justifyContent: "space-between" }}>
                                    <span>{x.name}</span>
                                    <span>
                    {x.value} ({pct}%)
                  </span>
                                </div>
                            );
                        })
                )}
            </div>
        </div>
    );
}
    
