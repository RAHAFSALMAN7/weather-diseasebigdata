const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");

const resultsRoutes = require("./routes/results");

const app = express();

// ======================
// Middleware
// ======================
app.use(cors());
app.use(express.json());

// ======================
// MongoDB Connection
// ======================
mongoose
  .connect("mongodb://localhost:27017/weather_disease_db")
  .then(() => console.log("✅ MongoDB connected"))
  .catch(err => console.error("❌ MongoDB error:", err));

// ======================
// Routes
// ======================
app.use("/api/results", resultsRoutes);

// Health check
app.get("/", (req, res) => {
  res.send("🌍 Weather–Disease Backend Running");
});

// ======================
// Start Server
// ======================
const PORT = 5000;
app.listen(PORT, () =>
  console.log(`🚀 Backend running on http://localhost:${PORT}`)
);
