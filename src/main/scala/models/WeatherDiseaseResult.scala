package models

case class WeatherDiseaseResult(
                                 country: String,
                                 season: String,
                                 disease: String,
                                 infection_rate: Double,
                                 event_date: String,          
                                 avg_temperature: Double,
                                 avg_humidity: Double,
                                 humidity_level: String,
                                 weather_stress_index: Int
                               )
