const mongoose = require("mongoose");

const WeatherDiseaseResultSchema = new mongoose.Schema(
    {
        country: { type: String, required: true },
        season: { type: String, required: true },
        disease: { type: String, required: true },


        avg_infection_rate: { type: Number, default: 0 },
        avg_temperature: { type: Number, default: 0 },
        avg_feels_like: { type: Number, default: 0 },
        avg_humidity: { type: Number, default: 0 },
        avg_wind_speed: { type: Number, default: 0 },
        avg_pressure: { type: Number, default: 0 },
        avg_cloudiness: { type: Number, default: 0 },

        humidity_level: { type: String, default: "" },
        weather_stress_index: { type: Number, default: 0 },

        ingested_at: { type: Date }
    },
    { timestamps: false }
);


module.exports = mongoose.model(
    "WeatherDiseaseResult",
    WeatherDiseaseResultSchema,
    "seasonal_analysis"
);
