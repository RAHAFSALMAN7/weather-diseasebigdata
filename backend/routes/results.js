const express = require("express");
const router = express.Router();

const WeatherDiseaseResult = require("../models/WeatherDiseaseResult");


function normalize(s) {
  if (!s) return "";
  return String(s).trim();
}


router.get("/", async (req, res) => {
  try {
    const limit = Math.min(parseInt(req.query.limit || "500", 10), 2000);

    const results = await WeatherDiseaseResult.find()
        .sort({ ingested_at: -1 })
        .limit(limit);

    res.json(results);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});


router.get("/country/:country", async (req, res) => {
  try {
    const country = normalize(req.params.country);


    const results = await WeatherDiseaseResult.find({
      country: { $regex: new RegExp(`^${country}$`, "i") }
    }).sort({ ingested_at: -1 });

    res.json(results);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});


router.get("/season/:season", async (req, res) => {
  try {
    const season = normalize(req.params.season);

    const results = await WeatherDiseaseResult.find({
      season: { $regex: new RegExp(`^${season}$`, "i") }
    }).sort({ ingested_at: -1 });

    res.json(results);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});


router.get("/disease/:disease", async (req, res) => {
  try {
    const disease = normalize(req.params.disease);

    const results = await WeatherDiseaseResult.find({
      disease: { $regex: new RegExp(`^${disease}$`, "i") }
    }).sort({ ingested_at: -1 });

    res.json(results);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});


router.get("/map", async (req, res) => {
  try {
    const results = await WeatherDiseaseResult.aggregate([
      {
        $group: {
          _id: "$country",
          avgInfectionRate: { $avg: "$avg_infection_rate" },
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
      },
      { $sort: { country: 1 } }
    ]);

    res.json(results);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
