import React, { useEffect, useMemo, useState } from "react";
import { fetchResults } from "../api/api";

import Filters from "../components/Filters";
import KpiRow from "../components/KpiRow";
import Charts from "../components/Charts";
import DonutCard from "../components/DonutCard";
import DataTable from "../components/DataTable";
import WorldMap from "../components/WorldMap";

export default function Dashboard() {
    const [raw, setRaw] = useState([]);
    const [loading, setLoading] = useState(true);
    const [errorMsg, setErrorMsg] = useState("");
    const [noteMsg, setNoteMsg] = useState("");

    const [country, setCountry] = useState("all");
    const [limit, setLimit] = useState(500);

    useEffect(() => {
        let mounted = true;

        (async () => {
            try {
                setLoading(true);
                setErrorMsg("");
                setNoteMsg("");

                const n = Math.max(1, parseInt(limit, 10) || 500);

                const params = { limit: n };
                if (country !== "all") params.country = country;

                const data = await fetchResults(params);

                if (!mounted) return;

                if (!Array.isArray(data)) {
                    setRaw([]);
                    setErrorMsg("API returned non-array response. Check backend response format.");
                } else {
                    setRaw(data);

                    if (n > 500 && data.length === 500) {
                        setNoteMsg(
                            "Backend seems to cap results at 500. Update the API to respect the limit query param."
                        );
                    }
                }
            } catch (e) {
                console.error("API ERROR:", e);
                if (!mounted) return;

                if (e?.code === "ECONNABORTED") {
                    setErrorMsg("Request timeout. Backend not responding (check localhost:5000).");
                } else if (e?.message?.includes("Network Error")) {
                    setErrorMsg("Network Error (likely CORS or backend down). Check console/network.");
                } else {
                    setErrorMsg(e?.message || "Unknown API error");
                }
                setRaw([]);
            } finally {
                if (!mounted) return;
                setLoading(false);
            }
        })();

        return () => {
            mounted = false;
        };
    }, [country, limit]);

    const options = useMemo(() => {
        const countries = Array.from(new Set(raw.map((r) => r.country).filter(Boolean))).sort();
        return { countries: ["all", ...countries] };
    }, [raw]);

    const cardStyle = {
        border: "1px solid var(--card-border)",
        borderRadius: "var(--radius)",
        padding: 14,
        background: "var(--card-bg)",
        boxShadow: "var(--shadow)",
    };

    return (
        <div style={{ padding: 16, minHeight: "100vh" }}>
            <div style={{ ...cardStyle, marginBottom: 12 }}>
                <h2 style={{ margin: 0 }}>Weather × Disease — Seasonal Dashboard</h2>
                <div style={{ marginTop: 10 }}>
                    <Filters
                        options={options}
                        value={{ country, limit }}
                        onChange={{ setCountry, setLimit }}
                        onReset={() => {
                            setCountry("all");
                            setLimit(500);
                        }}
                    />
                </div>
            </div>

            {loading && <p style={{ color: "var(--muted)" }}>Loading...</p>}

            {!loading && noteMsg && (
                <div
                    style={{
                        ...cardStyle,
                        borderLeft: "6px solid var(--warning)",
                        background: "rgba(242,153,74,0.10)",
                    }}
                >
                    <b>Note</b>
                    <div style={{ marginTop: 6 }}>{noteMsg}</div>
                </div>
            )}

            {!loading && errorMsg && (
                <div
                    style={{
                        ...cardStyle,
                        borderLeft: "6px solid var(--danger)",
                        background: "rgba(235,87,87,0.08)",
                    }}
                >
                    <b>Could not load data</b>
                    <div style={{ marginTop: 6 }}>{errorMsg}</div>
                </div>
            )}

            {!loading && !errorMsg && (
                <>
                    <KpiRow rows={raw} />

                    <div
                        style={{
                            display: "grid",
                            gridTemplateColumns: "repeat(auto-fit, minmax(300px, 1fr))",
                            gap: 12,
                            marginTop: 12,
                        }}
                    >
                        <DonutCard rows={raw} />
                        <WorldMap rows={raw} selectedCountry={country} />
                    </div>

                    <div style={{ marginTop: 12 }}>
                        <Charts rows={raw} selectedCountry={country} />
                    </div>

                    <div style={{ marginTop: 12 }}>
                        <DataTable rows={raw} />
                    </div>
                </>
            )}
        </div>
    );
}
