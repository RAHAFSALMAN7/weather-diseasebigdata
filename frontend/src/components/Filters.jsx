import React from "react";

export default function Filters({ options, value, onChange, onReset }) {
    const { countries } = options;
    const { country, limit } = value;

    const fieldStyle = {
        padding: "7px 10px",
        borderRadius: 10,
        border: "1px solid var(--card-border)",
        background: "#fff",
        outline: "none",
    };

    return (
        <div style={{ display: "flex", gap: 12, flexWrap: "wrap", alignItems: "center" }}>
            <label style={{ display: "flex", gap: 8, alignItems: "center", color: "var(--muted)" }}>
                <span>Country</span>
                <select
                    value={country}
                    onChange={(e) => onChange.setCountry(e.target.value)}
                    style={fieldStyle}
                >
                    {countries.map((c) => (
                        <option key={c} value={c}>
                            {c === "all" ? "All" : c}
                        </option>
                    ))}
                </select>
            </label>

            <label style={{ display: "flex", gap: 8, alignItems: "center", color: "var(--muted)" }}>
                <span>Show</span>
                <input
                    type="number"
                    min="1"
                    max="100000"
                    value={limit}
                    onChange={(e) => onChange.setLimit(e.target.value)}
                    style={{ ...fieldStyle, width: 120 }}
                />
            </label>

            <button
                onClick={onReset}
                style={{
                    padding: "8px 14px",
                    borderRadius: 10,
                    border: "none",
                    background: "var(--primary)",
                    color: "#fff",
                    cursor: "pointer",
                    boxShadow: "0 2px 8px rgba(47,128,237,0.25)",
                }}
            >
                Reset
            </button>
        </div>
    );
}
