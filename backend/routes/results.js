const express = require("express");
const router = express.Router();

const WeatherDiseaseResult = require("../models/WeatherDiseaseResult");

// =============================
// GET all results (limit for safety)
// =============================
router.get("/", async (req, res) => {
  try {
    const results = await WeatherDiseaseResult.find().limit(500);
    res.json(results);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// =============================
// GET by country
// =============================
router.get("/country/:country", async (req, res) => {
  try {
    const results = await WeatherDiseaseResult.find({
      country: req.params.country
    });
    res.json(results);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// =============================
// GET by season
// =============================
router.get("/season/:season", async (req, res) => {
  try {
    const results = await WeatherDiseaseResult.find({
      season: req.params.season
    });
    res.json(results);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// =============================
// GET data for map visualization
// =============================
router.get("/map", async (req, res) => {
  try {
    const results = await WeatherDiseaseResult.aggregate([
      {
        $group: {
          _id: "$country",
          avgInfectionRate: { $avg: "$infection_rate" },
          avgStressIndex: { $avg: "$weather_stress_index" }
        }
      },
      {
        $project: {
          _id: 0,
          country: "$_id",
          avgInfectionRate: 1,
          avgStressIndex: 1
        }
      }
    ]);

    res.json(results);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
