const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");

const resultsRoutes = require("./routes/results");

const app = express();


app.use(cors());
app.use(express.json());


const MONGO_URI = "mongodb://localhost:27017/weather_disease_db";

mongoose
    .connect(MONGO_URI)
    .then(() => console.log(" MongoDB connected"))
    .catch((err) => console.error(" MongoDB error:", err));


app.use("/api/results", resultsRoutes);


app.get("/health", (req, res) => {
  res.json({ status: "ok", server: "Weather-Disease Backend" });
});


const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`Backend running on http://localhost:${PORT}`);
});
