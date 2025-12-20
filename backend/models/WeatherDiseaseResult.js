const mongoose = require("mongoose");

const WeatherDiseaseResultSchema = new mongoose.Schema(
  {
    country: String,
    season: String,
    disease: String,

    infection_rate: Number,
    event_date: Date,

    avg_temperature: Number,
    avg_humidity: Number,

    humidity_level: String,
    weather_stress_index: Number
  },
  { timestamps: true }
);

 module.exports = mongoose.model(
  "WeatherDiseaseResult",
  WeatherDiseaseResultSchema,
  "weather_disease_results"
);
