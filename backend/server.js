// server.js
const express = require("express");
const mongoose = require("mongoose");
const resultsRoute = require("./routes/results");

const app = express();
app.use(express.json());

mongoose.connect("mongodb://localhost:27017/weather_disease_db");

app.use("/api/results", resultsRoute);

app.listen(5000, () => {
  console.log("Backend running on http://localhost:5000");
});
