import React, { useMemo } from "react";
import { ComposableMap, Geographies, Geography } from "react-simple-maps";

// TopoJSON جاهز للعالم (مستقر ومستخدم كثير)
const GEO_URL = "https://cdn.jsdelivr.net/npm/world-atlas@2/countries-110m.json";

function norm(s) {
    return (s || "").toString().trim().toLowerCase();
}

export default function WorldMap({ rows, selectedCountry }) {
    // الدول الموجودة عندك بالداتا
    const countriesInData = useMemo(() => {
        const set = new Set(rows.map(r => norm(r.country)).filter(Boolean));
        return set;
    }, [rows]);

    const selected = norm(selectedCountry);

    return (
        <div style={{ border: "1px solid #ddd", borderRadius: 12, padding: 12, background: "#fff" }}>
            <h3 style={{ margin: "0 0 8px 0" }}>World Map</h3>
            <div style={{ fontSize: 12, opacity: 0.8, marginBottom: 8 }}>
                Countries in your dataset are highlighted. Selected country gets a bold outline.
            </div>

            <div style={{ width: "100%", height: 260 }}>
                <ComposableMap projectionConfig={{ scale: 140 }} style={{ width: "100%", height: "100%" }}>
                    <Geographies geography={GEO_URL}>
                        {({ geographies }) =>
                            geographies.map((geo) => {
                                const name = norm(geo.properties?.name); // اسم الدولة بالـ topojson
                                const isInData = countriesInData.has(name);
                                const isSelected = selected !== "all" && selected && name === selected;

                                // ألوان هادية جداً (ماتأثرش على باقي UI)
                                const fill = isInData ? "#d9e6ff" : "#f2f2f2";
                                const stroke = isSelected ? "#111" : "#bbb";
                                const strokeWidth = isSelected ? 1.2 : 0.5;

                                return (
                                    <Geography
                                        key={geo.rsmKey}
                                        geography={geo}
                                        style={{
                                            default: { fill, stroke, strokeWidth, outline: "none" },
                                            hover: { fill, stroke, strokeWidth, outline: "none" },
                                            pressed: { fill, stroke, strokeWidth, outline: "none" }
                                        }}
                                    />
                                );
                            })
                        }
                    </Geographies>
                </ComposableMap>
            </div>
        </div>
    );
}
